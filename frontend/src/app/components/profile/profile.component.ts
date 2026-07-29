import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  authService = inject(AuthService);
  private fb = inject(FormBuilder);

  profileForm = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    phoneNumber: ['', [Validators.pattern(/^\+?[0-9\s-]{7,15}$/)]],
    bio: ['', [Validators.maxLength(500)]]
  });

  passwordForm = this.fb.group({
    oldPassword: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/)]],
    confirmPassword: ['', [Validators.required]]
  });

  isSavingProfile = signal(false);
  isChangingPassword = signal(false);
  isUploadingImage = signal(false);

  profileMessage = signal<string | null>(null);
  profileError = signal<string | null>(null);

  passwordMessage = signal<string | null>(null);
  passwordError = signal<string | null>(null);

  showPasswordDialog = signal(false);
  imagePreview = signal<string | null>(null);

  ngOnInit() {
    this.authService.loadUserProfile().subscribe({
      next: (user) => {
        this.profileForm.patchValue({
          firstName: user.firstName || '',
          lastName: user.lastName || '',
          phoneNumber: user.phoneNumber || '',
          bio: user.bio || ''
        });
        if (user.profileImageUrl) {
          this.imagePreview.set(user.profileImageUrl);
        }
      }
    });
  }

  onSaveProfile() {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSavingProfile.set(true);
    this.profileMessage.set(null);
    this.profileError.set(null);

    const data = this.profileForm.value;
    this.authService.updateProfile(data).subscribe({
      next: () => {
        this.isSavingProfile.set(false);
        this.profileMessage.set('Profile updated successfully!');
        setTimeout(() => this.profileMessage.set(null), 3500);
      },
      error: (err) => {
        this.isSavingProfile.set(false);
        this.profileError.set(err.error?.message || 'Failed to update profile.');
      }
    });
  }

  onChangePassword() {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    if (this.passwordForm.value.newPassword !== this.passwordForm.value.confirmPassword) {
      this.passwordError.set('New passwords do not match.');
      return;
    }

    this.isChangingPassword.set(true);
    this.passwordMessage.set(null);
    this.passwordError.set(null);

    const payload = {
      oldPassword: this.passwordForm.value.oldPassword,
      newPassword: this.passwordForm.value.newPassword
    };

    this.authService.changePassword(payload).subscribe({
      next: (res) => {
        this.isChangingPassword.set(false);
        this.passwordMessage.set(res.message || 'Password changed successfully!');
        this.passwordForm.reset();
        setTimeout(() => {
          this.passwordMessage.set(null);
          this.showPasswordDialog.set(false);
        }, 2000);
      },
      error: (err) => {
        this.isChangingPassword.set(false);
        this.passwordError.set(err.error?.message || 'Failed to change password. Please verify current password.');
      }
    });
  }

  onImageSelected(event: Event) {
    const fileInput = event.target as HTMLInputElement;
    if (fileInput.files && fileInput.files[0]) {
      const file = fileInput.files[0];
      const reader = new FileReader();
      
      reader.onload = () => {
        const base64 = reader.result as string;
        this.imagePreview.set(base64);
        
        this.isUploadingImage.set(true);
        this.authService.uploadProfileImage(base64).subscribe({
          next: () => {
            this.isUploadingImage.set(false);
            this.profileMessage.set('Profile image updated!');
            setTimeout(() => this.profileMessage.set(null), 3500);
          },
          error: () => {
            this.isUploadingImage.set(false);
            this.profileError.set('Failed to upload image.');
          }
        });
      };

      reader.readAsDataURL(file);
    }
  }

  isFieldInvalid(formName: 'profile' | 'password', fieldName: string): boolean {
    const control = formName === 'profile' 
      ? this.profileForm.get(fieldName as any) 
      : this.passwordForm.get(fieldName as any);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
