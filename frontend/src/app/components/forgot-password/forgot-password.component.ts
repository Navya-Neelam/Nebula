import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  message: string | null = null;
  error: string | null = null;
  isLoading = signal(false);

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const email = this.form.value.email?.trim() ?? '';

    this.error = null;
    this.isLoading.set(true);
    this.auth.forgotPassword({ email }).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.message = res.message || 'OTP sent successfully';
        sessionStorage.setItem('resetEmail', email);
        setTimeout(() => this.router.navigate(['/verify-otp']), 800);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.error = err.error?.message || 'Unable to process request';
      }
    });
  }

  isFieldInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
