import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify-email.component.html'
})
export class VerifyEmailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private auth = inject(AuthService);

  isVerifying = signal(true);
  isSuccess = signal(false);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    const token = this.route.snapshot.queryParams['token'];
    if (!token) {
      this.isVerifying.set(false);
      this.isSuccess.set(false);
      this.errorMessage.set('No verification token was found. Please check your verification link.');
      return;
    }

    this.auth.verifyEmail(token).subscribe({
      next: () => {
        this.isVerifying.set(false);
        this.isSuccess.set(true);
      },
      error: (err) => {
        this.isVerifying.set(false);
        this.isSuccess.set(false);
        this.errorMessage.set(err.error?.message || 'Verification failed. The token may be invalid or expired.');
      }
    });
  }
}
