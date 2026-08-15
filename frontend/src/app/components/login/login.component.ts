import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['token']) {
        const token = params['token'];
        const refreshToken = params['refreshToken'] || '';
        const role = params['role'] || 'STUDENT';
        const id = params['id'] || '';
        const fullName = params['fullName'] || '';
        const email = params['email'] || '';

        sessionStorage.setItem('token', token);
        sessionStorage.setItem('refreshToken', refreshToken);
        const user = {
          id,
          fullName,
          email,
          createdAt: '',
          role: role as any,
          isVerified: true,
          isActive: true
        };
        sessionStorage.setItem('user', JSON.stringify(user));
        this.authService.currentUser.set(user);
        this.router.navigate(['/dashboard']);
      } else if (params['error']) {
        this.errorMessage.set(params['error']);
      }
    });
  }

  loginWithGoogle() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  }

  loginWithGitHub() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/github';
  }

  loginTab = signal<'password' | 'otp'>('password');

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    rememberMe: [false]
  });

  otpForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    otp: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]],
    rememberMe: [false]
  });

  showPassword = signal(false);
  isLoading = signal(false);
  isSendingOtp = signal(false);
  otpSent = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  otpCountdown = signal(300); // 5 minutes in seconds
  resendCooldown = signal(0);  // 60 seconds resend cooldown
  private timerInterval: any;
  private cooldownInterval: any;

  switchTab(tab: 'password' | 'otp') {
    this.loginTab.set(tab);
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  togglePassword() {
    this.showPassword.update(v => !v);
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.errorMessage.set(err.error?.message || 'Invalid email or password');
        } else {
          this.errorMessage.set(err.error?.message || 'An unexpected error occurred. Please try again.');
        }
      }
    });
  }

  sendOtp() {
    const emailControl = this.otpForm.get('email');
    if (!emailControl || emailControl.invalid) {
      emailControl?.markAsTouched();
      return;
    }

    this.isSendingOtp.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService.sendLoginOtp({ email: emailControl.value }).subscribe({
      next: (res) => {
        this.isSendingOtp.set(false);
        this.otpSent.set(true);
        this.successMessage.set('6-digit OTP code sent! Check your inbox.');
        this.startCountdown();
        this.startResendCooldown();
      },
      error: (err) => {
        this.isSendingOtp.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to send OTP. Please verify your email.');
      }
    });
  }

  verifyOtpLogin() {
    if (this.otpForm.invalid) {
      this.otpForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.verifyLoginOtp(this.otpForm.value).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Invalid or expired OTP code.');
      }
    });
  }

  private startCountdown() {
    clearInterval(this.timerInterval);
    this.otpCountdown.set(300);
    this.timerInterval = setInterval(() => {
      const current = this.otpCountdown();
      if (current > 1) {
        this.otpCountdown.set(current - 1);
      } else {
        this.otpCountdown.set(0);
        clearInterval(this.timerInterval);
      }
    }, 1000);
  }

  private startResendCooldown() {
    clearInterval(this.cooldownInterval);
    this.resendCooldown.set(60);
    this.cooldownInterval = setInterval(() => {
      const current = this.resendCooldown();
      if (current > 1) {
        this.resendCooldown.set(current - 1);
      } else {
        this.resendCooldown.set(0);
        clearInterval(this.cooldownInterval);
      }
    }, 1000);
  }

  formatTimer(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  isFieldInvalid(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  isOtpFieldInvalid(field: string): boolean {
    const control = this.otpForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
