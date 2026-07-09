import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export interface User {
  id: string;
  fullName: string;
  email: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  id: string;
  fullName: string;
  email: string;
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

  constructor() {
    if (sessionStorage.getItem('token')) {
      this.loadUserProfile().subscribe({
        error: () => this.logout()
      });
    }
  }

  forgotPassword(email: { email: string }) {
    return this.http.post<{ message: string }>(`${this.apiUrl}/forgot-password`, email);
  }

  verifyOtp(payload: { email: string; otp: string }) {
    return this.http.post<{ message: string }>(`${this.apiUrl}/verify-otp`, payload);
  }

  resetPassword(payload: { email: string; otp: string; newPassword: string; confirmPassword: string }) {
    return this.http.post<{ message: string }>(`${this.apiUrl}/reset-password`, payload);
  }

  register(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, userData).pipe(
      tap(res => this.handleAuthSuccess(res))
    );
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => this.handleAuthSuccess(res))
    );
  }

  loadUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      tap(user => this.currentUser.set(user))
    );
  }

  logout() {
    sessionStorage.removeItem('token');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  private handleAuthSuccess(res: AuthResponse) {
    sessionStorage.setItem('token', res.token);
    this.currentUser.set({
      id: res.id,
      fullName: res.fullName,
      email: res.email,
      createdAt: ''
    });
  }
}
