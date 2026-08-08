export interface UserPreferences {
  theme: string; // 'DARK' | 'LIGHT' | 'SYSTEM'
  language: string;
  timeZone: string;
  emailNotifications: boolean;
  marketingEmails: boolean;
}

export interface OnboardingStatus {
  registrationCompleted: boolean;
  welcomeScreenViewed: boolean;
  preferencesSaved: boolean;
  profileCompleted: boolean;
}

export interface ProfileCompletion {
  completionPercentage: number;
  completedItems: string[];
  pendingItems: string[];
}

export interface MultiStepRegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  phone?: string;
  country?: string;
  timeZone?: string;
  userPreferences?: Partial<UserPreferences>;
}
