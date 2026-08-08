import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserPreferencesDTO {
  theme?: string;
  language?: string;
  timeZone?: string;
  emailNotifications?: boolean;
  marketingEmails?: boolean;
}

export interface ProfileCompletionDTO {
  completionPercentage: number;
  totalFieldsCount: number;
  completedFieldsCount: number;
  completedFields: string[];
  missingFields: string[];
}

export interface OnboardingStatusDTO {
  registrationCompleted: boolean;
  welcomeScreenViewed: boolean;
  preferencesSaved: boolean;
  profileCompleted: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class PreferencesService {
  private http = inject(HttpClient);
  private prefUrl = 'http://localhost:8080/api/preferences';
  private profileUrl = 'http://localhost:8080/api/profile';
  private onboardingUrl = 'http://localhost:8080/api/onboarding';

  getPreferences(): Observable<UserPreferencesDTO> {
    return this.http.get<UserPreferencesDTO>(this.prefUrl);
  }

  updatePreferences(dto: UserPreferencesDTO): Observable<UserPreferencesDTO> {
    return this.http.put<UserPreferencesDTO>(this.prefUrl, dto);
  }

  getProfileCompletion(): Observable<ProfileCompletionDTO> {
    return this.http.get<ProfileCompletionDTO>(`${this.profileUrl}/completion`);
  }

  getOnboardingStatus(): Observable<OnboardingStatusDTO> {
    return this.http.get<OnboardingStatusDTO>(`${this.onboardingUrl}/status`);
  }

  updateOnboardingStatus(dto: Partial<OnboardingStatusDTO>): Observable<OnboardingStatusDTO> {
    return this.http.put<OnboardingStatusDTO>(`${this.onboardingUrl}/status`, dto);
  }
}
