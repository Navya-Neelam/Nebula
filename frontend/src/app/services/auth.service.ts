import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

// Role type definitions
export type UserRole = 'ADMIN' | 'INSTRUCTOR' | 'STUDENT';

export interface User {
  id: string;
  fullName: string;
  email: string;
  createdAt: string;
  role: UserRole;
  isVerified: boolean;
  isActive: boolean;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  bio?: string;
  profileImageUrl?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  role: UserRole;
  id: string;
  fullName: string;
  email: string;
}

export interface LoginHistoryItem {
  id: string;
  userId: string;
  email: string;
  ipAddress: string;
  device: string;
  browser: string;
  os?: string;
  location?: string;
  loginMethod: string;
  loginTime: string;
  status: 'SUCCESS' | 'FAILED';
  userAgent: string;
}

export interface ActiveSession {
  id: string;
  device: string;
  browser: string;
  os?: string;
  location?: string;
  ipAddress: string;
  createdAt: string;
  expiresAt: string;
  currentSession: boolean;
  rememberMe: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = 'http://localhost:8080/api/auth';

  currentUser = signal<User | null>(null);
  isAuthenticated = computed(() => this.currentUser() !== null);
  userRole = computed(() => this.currentUser()?.role || null);

  constructor() {
    // Restore cached user synchronously so route guards don't fail during reload
    const storedUserStr = localStorage.getItem('user') || sessionStorage.getItem('user');
    if (storedUserStr) {
      try {
        this.currentUser.set(JSON.parse(storedUserStr));
      } catch (e) {}
    }

    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (token) {
      this.loadUserProfile().subscribe({
        error: (err) => {
          if (err.status === 401 || err.status === 403) {
            this.logout();
          }
        }
      });
    }
  }

  forgotPassword(email: { email: string }) {
    return this.http.post<{ message: string }>(`${this.apiUrl}/forgot-password`, email);
  }

  verifyOtp(payload: { email: string; otp: string }): Observable<{ message: string; resetToken: string }> {
    return this.http.post<{ message: string; resetToken: string }>(`${this.apiUrl}/verify-otp`, payload);
  }

  resetPassword(payload: { resetToken: string; newPassword: string; confirmPassword: string }) {
    return this.http.post<{ message: string }>(`${this.apiUrl}/reset-password`, payload);
  }

  verifyEmail(token: string): Observable<{ message: string }> {
    return this.http.get<{ message: string }>(`${this.apiUrl}/verify?token=${token}`);
  }

  register(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, userData);
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => this.handleAuthSuccess(res, !!credentials?.rememberMe))
    );
  }

  sendLoginOtp(payload: { email: string }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/send-login-otp`, payload);
  }

  verifyLoginOtp(payload: { email: string; otp: string; rememberMe?: boolean }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/verify-login-otp`, payload).pipe(
      tap(res => this.handleAuthSuccess(res, !!payload?.rememberMe))
    );
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = sessionStorage.getItem('refreshToken') || localStorage.getItem('refreshToken') || '';
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh-token`, { refreshToken }).pipe(
      tap(res => {
        sessionStorage.setItem('token', res.token);
        if (localStorage.getItem('token')) {
          localStorage.setItem('token', res.token);
        }
        if (res.refreshToken) {
          sessionStorage.setItem('refreshToken', res.refreshToken);
          if (localStorage.getItem('refreshToken')) {
            localStorage.setItem('refreshToken', res.refreshToken);
          }
        }
      })
    );
  }

  loadUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      tap(user => {
        this.currentUser.set(user);
        sessionStorage.setItem('user', JSON.stringify(user));
        if (localStorage.getItem('user')) {
          localStorage.setItem('user', JSON.stringify(user));
        }
      })
    );
  }

  updateProfile(profileData: any): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/profile`, profileData).pipe(
      tap(user => {
        this.currentUser.set(user);
        sessionStorage.setItem('user', JSON.stringify(user));
        if (localStorage.getItem('user')) {
          localStorage.setItem('user', JSON.stringify(user));
        }
      })
    );
  }

  changePassword(passwordData: any): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/change-password`, passwordData);
  }

  uploadProfileImage(imageBase64: string): Observable<{ imageUrl: string }> {
    return this.http.post<{ imageUrl: string }>(`${this.apiUrl}/profile/upload-image`, { image: imageBase64 }).pipe(
      tap(res => {
        const current = this.currentUser();
        if (current) {
          const updated = {
            ...current,
            profileImageUrl: res.imageUrl
          };
          this.currentUser.set(updated);
          sessionStorage.setItem('user', JSON.stringify(updated));
          if (localStorage.getItem('user')) {
            localStorage.setItem('user', JSON.stringify(updated));
          }
        }
      })
    );
  }

  logout() {
    const refreshToken = sessionStorage.getItem('refreshToken') || localStorage.getItem('refreshToken');
    if (refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe({
        next: () => this.clearSessionAndRedirect(),
        error: () => this.clearSessionAndRedirect()
      });
    } else {
      this.clearSessionAndRedirect();
    }
  }

  private clearSessionAndRedirect() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('refreshToken');
    sessionStorage.removeItem('user');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  private handleAuthSuccess(res: AuthResponse, rememberMe: boolean = false) {
    sessionStorage.setItem('token', res.token);
    sessionStorage.setItem('refreshToken', res.refreshToken);

    if (rememberMe) {
      localStorage.setItem('token', res.token);
      localStorage.setItem('refreshToken', res.refreshToken);
    } else {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }

    const user: User = {
      id: res.id,
      fullName: res.fullName,
      email: res.email,
      createdAt: '',
      role: res.role,
      isVerified: true,
      isActive: true
    };

    sessionStorage.setItem('user', JSON.stringify(user));
    if (rememberMe) {
      localStorage.setItem('user', JSON.stringify(user));
    }
    this.currentUser.set(user);
  }

  hasRole(roles: string[]): boolean {
    const role = this.userRole();
    if (!role) return false;
    return roles.includes(role);
  }

  getLoginHistory(): Observable<LoginHistoryItem[]> {
    return this.http.get<LoginHistoryItem[]>('http://localhost:8080/api/login/history');
  }

  getCurrentSessions(): Observable<ActiveSession[]> {
    const refreshToken = sessionStorage.getItem('refreshToken') || localStorage.getItem('refreshToken') || '';
    return this.http.get<ActiveSession[]>('http://localhost:8080/api/login/current-session', {
      headers: { 'X-Refresh-Token': refreshToken }
    });
  }

  revokeSession(sessionId: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`http://localhost:8080/api/login/session/${sessionId}`);
  }

  revokeAllSessions(): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>('http://localhost:8080/api/login/logout-all');
  }
}
