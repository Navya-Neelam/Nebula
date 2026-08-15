import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService, LoginHistoryItem, ActiveSession } from '../../services/auth.service';

@Component({
  selector: 'app-login-history',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './login-history.component.html',
  styleUrl: './login-history.component.css'
})
export class LoginHistoryComponent implements OnInit {
  private authService = inject(AuthService);

  activeSessions = signal<ActiveSession[]>([]);
  loginHistory = signal<LoginHistoryItem[]>([]);
  isLoadingSessions = signal<boolean>(true);
  isLoadingHistory = signal<boolean>(true);
  actionMessage = signal<{ type: 'success' | 'error'; text: string } | null>(null);
  isRevokingAll = signal<boolean>(false);
  revokingSessionId = signal<string | null>(null);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loadSessions();
    this.loadHistory();
  }

  loadSessions() {
    this.isLoadingSessions.set(true);
    this.authService.getCurrentSessions().subscribe({
      next: (sessions) => {
        this.activeSessions.set(sessions);
        this.isLoadingSessions.set(false);
      },
      error: (err) => {
        console.error('Failed to load active sessions', err);
        this.isLoadingSessions.set(false);
      }
    });
  }

  loadHistory() {
    this.isLoadingHistory.set(true);
    this.authService.getLoginHistory().subscribe({
      next: (history) => {
        this.loginHistory.set(history);
        this.isLoadingHistory.set(false);
      },
      error: (err) => {
        console.error('Failed to load login history', err);
        this.isLoadingHistory.set(false);
      }
    });
  }

  revokeSession(sessionId: string) {
    if (confirm('Are you sure you want to revoke this session? You will be logged out from that device.')) {
      this.revokingSessionId.set(sessionId);
      this.authService.revokeSession(sessionId).subscribe({
        next: (res) => {
          this.showMessage('success', res.message || 'Session revoked successfully');
          this.revokingSessionId.set(null);
          this.loadSessions();
        },
        error: (err) => {
          this.showMessage('error', err.error?.message || 'Failed to revoke session');
          this.revokingSessionId.set(null);
        }
      });
    }
  }

  logoutAllSessions() {
    if (confirm('Are you sure you want to revoke ALL active sessions? This will log out all devices.')) {
      this.isRevokingAll.set(true);
      this.authService.revokeAllSessions().subscribe({
        next: (res) => {
          this.showMessage('success', res.message || 'All sessions revoked');
          this.isRevokingAll.set(false);
          this.authService.logout();
        },
        error: (err) => {
          this.showMessage('error', err.error?.message || 'Failed to revoke sessions');
          this.isRevokingAll.set(false);
        }
      });
    }
  }

  private showMessage(type: 'success' | 'error', text: string) {
    this.actionMessage.set({ type, text });
    setTimeout(() => this.actionMessage.set(null), 4000);
  }

  getDeviceIcon(device: string): string {
    const d = (device || '').toLowerCase();
    if (d.includes('mobile') || d.includes('phone') || d.includes('android') || d.includes('iphone')) return 'bi-phone';
    if (d.includes('tablet') || d.includes('ipad')) return 'bi-tablet';
    return 'bi-display';
  }
}
