import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OnboardingStatus, ProfileCompletion, UserPreferences } from '../models/onboarding.models';

@Injectable({
  providedIn: 'root'
})
export class OnboardingService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  getProfileCompletion(): Observable<ProfileCompletion> {
    return this.http.get<ProfileCompletion>(`${this.baseUrl}/profile/completion`);
  }

  getOnboardingStatus(): Observable<OnboardingStatus> {
    return this.http.get<OnboardingStatus>(`${this.baseUrl}/onboarding/status`);
  }

  updateOnboardingStatus(status: Partial<OnboardingStatus>): Observable<OnboardingStatus> {
    return this.http.put<OnboardingStatus>(`${this.baseUrl}/onboarding/status`, status);
  }

  getPreferences(): Observable<UserPreferences> {
    return this.http.get<UserPreferences>(`${this.baseUrl}/preferences`);
  }

  updatePreferences(preferences: Partial<UserPreferences>): Observable<UserPreferences> {
    return this.http.put<UserPreferences>(`${this.baseUrl}/preferences`, preferences);
  }
}
