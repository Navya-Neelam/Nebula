import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/)]],
    confirmPassword: ['', [Validators.required]]
  });

  token = signal<string | null>(null);
  message: string | null = null;
  error: string | null = null;
  showNewPassword = signal(false);
  showConfirmPassword = signal(false);
  isLoading = signal(false);

  ngOnInit() {
    const t = this.route.snapshot.queryParams['token'];
    if (t) {
      this.token.set(t);
    } else {
      const fallback = sessionStorage.getItem('resetToken');
      if (fallback) {
        this.token.set(fallback);
      }
    }
  }

  toggleNewPassword() {
    this.showNewPassword.update(v => !v);
  }

  toggleConfirmPassword() {
    this.showConfirmPassword.update(v => !v);
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.form.value.newPassword !== this.form.value.confirmPassword) {
      this.error = 'Passwords do not match';
      return;
    }

    const resetToken = this.token() ?? '';
    const newPassword = this.form.value.newPassword ?? '';
    const confirmPassword = this.form.value.confirmPassword ?? '';

    if (!resetToken) {
      this.error = 'Invalid or missing reset token. Please request a new link.';
      return;
    }

    this.error = null;
    this.isLoading.set(true);
    this.auth.resetPassword({ resetToken, newPassword, confirmPassword }).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.message = res.message || 'Password reset successful';
        sessionStorage.removeItem('resetEmail');
        sessionStorage.removeItem('otpVerifiedEmail');
        sessionStorage.removeItem('resetToken');
        setTimeout(() => this.router.navigate(['/login']), 1200);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.error = err.error?.message || 'Unable to reset password';
      }
    });
  }

  isFieldInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
