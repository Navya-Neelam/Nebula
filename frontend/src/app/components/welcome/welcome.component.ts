import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { OnboardingService } from '../../services/onboarding.service';
import { ProfileCompletion } from '../../models/onboarding.models';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent implements OnInit {
  private router = inject(Router);
  private onboardingService = inject(OnboardingService);

  firstName = signal<string>('Explorer');
  completionData = signal<ProfileCompletion | null>(null);
  isLoading = signal<boolean>(true);

  ngOnInit() {
    // Read from session storage or service
    const storedName = sessionStorage.getItem('registered_user_firstname');
    if (storedName) {
      this.firstName.set(storedName);
    }

    // Try fetching profile completion stats from backend
    this.onboardingService.getProfileCompletion().subscribe({
      next: (res) => {
        this.completionData.set(res);
        this.isLoading.set(false);

        // Mark welcome screen viewed in onboarding status
        this.onboardingService.updateOnboardingStatus({ welcomeScreenViewed: true }).subscribe();
      },
      error: () => {
        // Fallback default calculation if user hasn't logged in yet
        this.completionData.set({
          completionPercentage: 70,
          completedItems: ['Basic Information', 'Phone Added', 'Preferences Saved'],
          pendingItems: ['Profile Picture', 'Email Verified']
        });
        this.isLoading.set(false);
      }
    });
  }

  onGetStarted() {
    this.router.navigate(['/login']);
  }
}
