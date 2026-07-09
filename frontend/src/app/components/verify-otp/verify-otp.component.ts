import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './verify-otp.component.html'
})
export class VerifyOtpComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    otp: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]]
  });

  message: string | null = null;
  error: string | null = null;
  isLoading = signal(false);

  constructor() {
    const resetEmail = sessionStorage.getItem('resetEmail');
    if (resetEmail) {
      this.form.patchValue({ email: resetEmail });
    }
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const email = this.form.value.email?.trim() ?? '';
    const otp = this.form.value.otp?.trim() ?? '';

    this.error = null;
    this.isLoading.set(true);
    this.auth.verifyOtp({ email, otp }).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.message = res.message || 'OTP Verified';
        sessionStorage.setItem('otpVerifiedEmail', email);
        setTimeout(() => this.router.navigate(['/reset-password']), 800);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.error = err.error?.message || 'Unable to verify OTP';
      }
    });
  }

  isFieldInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
