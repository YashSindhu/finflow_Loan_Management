import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { inject } from '@angular/core';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule, NavbarComponent],
  templateUrl: './profile.html'
})
export class ProfileComponent implements OnInit {
  profile: any = null;
  loading = true;
  error = '';
  success = '';

  profileForm = { name: '', email: '' };
  updatingProfile = false;

  passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
  changingPassword = false;
  showOld = false;
  showNew = false;
  showConfirm = false;

  auth = inject(AuthService);
  theme = inject(ThemeService);
  private router = inject(Router);

  get isDark(): boolean { return this.theme.isDark; }
  toggleTheme(): void { this.theme.toggle(); }

  ngOnInit(): void {
    // Pre-fill from localStorage immediately so UI shows right away
    this.profileForm.name = this.auth.getEmail()?.split('@')[0] || '';
    this.profileForm.email = this.auth.getEmail() || '';
    this.profile = { name: this.profileForm.name, email: this.profileForm.email, role: this.auth.getRole() };
    this.loading = false;

    // Then try to load full profile from backend
    this.auth.getProfile().subscribe({
      next: (data) => {
        this.profile = data;
        this.profileForm.name = data.name;
        this.profileForm.email = data.email;
      },
      error: () => {
        // Backend not available — use localStorage data, still show the page
        this.error = '';
      }
    });
  }

  updateProfile(): void {
    this.error = '';
    this.success = '';
    this.updatingProfile = true;
    this.auth.updateProfile(this.profileForm.name, this.profileForm.email).subscribe({
      next: (data) => {
        this.updatingProfile = false;
        this.profile = data;
        // Update localStorage if email changed
        if (data.email !== this.auth.getEmail()) {
          localStorage.setItem('email', data.email);
        }
        this.success = 'Profile updated successfully!';
      },
      error: (err) => {
        this.updatingProfile = false;
        this.error = err.error?.message || err.error || 'Failed to update profile';
      }
    });
  }

  changePassword(): void {
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.error = 'New passwords do not match';
      return;
    }
    this.error = '';
    this.success = '';
    this.changingPassword = true;
    this.auth.changePassword(this.passwordForm.oldPassword, this.passwordForm.newPassword).subscribe({
      next: () => {
        this.changingPassword = false;
        this.success = 'Password changed successfully! Please login again.';
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
        setTimeout(() => {
          this.auth.logout();
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.changingPassword = false;
        this.error = err.error?.message || err.error || 'Failed to change password';
      }
    });
  }

  getRoleLabel(): string {
    const map: Record<string, string> = {
      'ROLE_USER': 'Regular User',
      'ROLE_ADMIN': 'Admin',
      'ROLE_SUPER_ADMIN': 'Super Admin'
    };
    return map[this.profile?.role] || this.profile?.role;
  }
}
