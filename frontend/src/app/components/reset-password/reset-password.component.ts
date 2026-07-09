import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    otp: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
    newPassword: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/)]],
    confirmPassword: ['', [Validators.required]]
  });

  message: string | null = null;
  error: string | null = null;
  isLoading = signal(false);

  constructor() {
    const resetEmail = sessionStorage.getItem('resetEmail');
    const verifiedEmail = sessionStorage.getItem('otpVerifiedEmail');
    const email = verifiedEmail || resetEmail || '';
    if (email) {
      this.form.patchValue({ email });
    }
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

    const email = this.form.value.email?.trim() ?? '';
    const otp = this.form.value.otp?.trim() ?? '';
    const newPassword = this.form.value.newPassword ?? '';
    const confirmPassword = this.form.value.confirmPassword ?? '';

    this.error = null;
    this.isLoading.set(true);
    this.auth.resetPassword({ email, otp, newPassword, confirmPassword }).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.message = res.message || 'Password reset successful';
        sessionStorage.removeItem('resetEmail');
        sessionStorage.removeItem('otpVerifiedEmail');
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
