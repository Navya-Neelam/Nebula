import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password');
  const confirmPassword = control.get('confirmPassword');

  if (password && confirmPassword && password.value !== confirmPassword.value) {
    confirmPassword.setErrors({ passwordMismatch: true });
    return { passwordMismatch: true };
  }
  
  return null;
};

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  currentStep = signal<number>(1);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  showPassword = signal<boolean>(false);
  showConfirmPassword = signal<boolean>(false);

  // Countries and Timezones lists
  countries = [
    { code: 'US', name: 'United States' },
    { code: 'CA', name: 'Canada' },
    { code: 'GB', name: 'United Kingdom' },
    { code: 'IN', name: 'India' },
    { code: 'DE', name: 'Germany' },
    { code: 'FR', name: 'France' },
    { code: 'AU', name: 'Australia' },
    { code: 'JP', name: 'Japan' },
    { code: 'BR', name: 'Brazil' },
    { code: 'SG', name: 'Singapore' }
  ];

  timezones = [
    'UTC',
    'America/New_York (EST)',
    'America/Chicago (CST)',
    'America/Los_Angeles (PST)',
    'Europe/London (GMT)',
    'Europe/Paris (CET)',
    'Asia/Kolkata (IST)',
    'Asia/Tokyo (JST)',
    'Australia/Sydney (AEST)'
  ];

  languages = [
    { code: 'en', name: 'English' },
    { code: 'es', name: 'Spanish' },
    { code: 'fr', name: 'French' },
    { code: 'de', name: 'German' },
    { code: 'ja', name: 'Japanese' },
    { code: 'hi', name: 'Hindi' }
  ];

  // Forms for each step
  step1Form: FormGroup = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]]
  });

  step2Form: FormGroup = this.fb.group({
    password: ['', [
      Validators.required, 
      Validators.minLength(8), 
      Validators.pattern('.*[0-9].*')
    ]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: passwordMatchValidator });

  step3Form: FormGroup = this.fb.group({
    phone: ['', [Validators.required, Validators.pattern('^[+]?[0-9\\s-]{7,15}$')]],
    country: ['US', [Validators.required]],
    timeZone: ['UTC', [Validators.required]]
  });

  step4Form: FormGroup = this.fb.group({
    theme: ['SYSTEM', [Validators.required]],
    language: ['en', [Validators.required]],
    emailNotifications: [true],
    marketingEmails: [false]
  });

  // Password strength logic
  passwordValue = signal<string>('');

  constructor() {
    this.step2Form.get('password')?.valueChanges.subscribe(val => {
      this.passwordValue.set(val || '');
    });
  }

  passwordStrength = computed(() => {
    const val = this.passwordValue();
    if (!val) return { score: 0, label: 'Very Weak', color: '#ef4444', percent: 0 };
    let score = 0;
    if (val.length >= 8) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[a-z]/.test(val) && /[A-Z]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    switch (score) {
      case 1:
        return { score: 1, label: 'Weak', color: '#ef4444', percent: 25 };
      case 2:
        return { score: 2, label: 'Fair', color: '#f59e0b', percent: 50 };
      case 3:
        return { score: 3, label: 'Good', color: '#3b82f6', percent: 75 };
      case 4:
        return { score: 4, label: 'Strong', color: '#10b981', percent: 100 };
      default:
        return { score: 0, label: 'Very Weak', color: '#ef4444', percent: 10 };
    }
  });

  togglePassword() {
    this.showPassword.update(v => !v);
  }

  toggleConfirmPassword() {
    this.showConfirmPassword.update(v => !v);
  }

  isStepValid(step: number): boolean {
    switch (step) {
      case 1: return this.step1Form.valid;
      case 2: return this.step2Form.valid;
      case 3: return this.step3Form.valid;
      case 4: return this.step4Form.valid;
      case 5: return this.step1Form.valid && this.step2Form.valid && this.step3Form.valid && this.step4Form.valid;
      default: return false;
    }
  }

  nextStep() {
    const current = this.currentStep();
    if (current === 1 && this.step1Form.invalid) {
      this.step1Form.markAllAsTouched();
      return;
    }
    if (current === 2 && this.step2Form.invalid) {
      this.step2Form.markAllAsTouched();
      return;
    }
    if (current === 3 && this.step3Form.invalid) {
      this.step3Form.markAllAsTouched();
      return;
    }
    if (current === 4 && this.step4Form.invalid) {
      this.step4Form.markAllAsTouched();
      return;
    }
    if (current < 5) {
      this.errorMessage.set(null);
      this.currentStep.set(current + 1);
    }
  }

  prevStep() {
    const current = this.currentStep();
    if (current > 1) {
      this.errorMessage.set(null);
      this.currentStep.set(current - 1);
    }
  }

  goToStep(step: number) {
    if (step < this.currentStep() || this.isStepValid(step - 1)) {
      this.errorMessage.set(null);
      this.currentStep.set(step);
    }
  }

  selectTheme(theme: string) {
    this.step4Form.patchValue({ theme });
  }

  isFieldInvalid(form: FormGroup, field: string): boolean {
    const control = form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  onSubmit() {
    if (!this.isStepValid(5)) {
      this.errorMessage.set('Please make sure all steps are valid before submitting.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const step1 = this.step1Form.value;
    const step2 = this.step2Form.value;
    const step3 = this.step3Form.value;
    const step4 = this.step4Form.value;

    const registrationPayload = {
      firstName: step1.firstName,
      lastName: step1.lastName,
      fullName: `${step1.firstName} ${step1.lastName}`.trim(),
      email: step1.email,
      password: step2.password,
      phone: step3.phone,
      country: step3.country,
      timeZone: step3.timeZone,
      userPreferences: {
        theme: step4.theme,
        language: step4.language,
        timeZone: step3.timeZone,
        emailNotifications: step4.emailNotifications,
        marketingEmails: step4.marketingEmails
      }
    };

    this.authService.register(registrationPayload).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        // Store registered user name for Welcome experience
        sessionStorage.setItem('registered_user_firstname', step1.firstName);
        sessionStorage.setItem('registered_user_email', step1.email);
        this.router.navigate(['/welcome']);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 409) {
          this.errorMessage.set('An account with this email already exists.');
          this.currentStep.set(1); // Jump to email step
        } else {
          this.errorMessage.set(err.error?.message || 'Registration failed. Please review your details and try again.');
        }
      }
    });
  }
}
