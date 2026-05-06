import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html'
})
export class RegisterComponent {
  form = { name: '', email: '', password: '' };
  loading = false;
  error = '';
  success = '';
  showPassword = false;
  showConfirm = false;
  confirmPassword = '';

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit(): void {
    this.error = '';
    this.success = '';
    this.loading = true;

    this.auth.register(this.form).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Account created! Redirecting to login...';
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.error = 'Cannot connect to server. Make sure backend is running on port 8080.';
        } else if (err.error?.errors?.length) {
          this.error = err.error.errors.map((e: any) => e.defaultMessage || e.message).join(', ');
        } else {
          this.error = err.error?.message || err.error || 'Registration failed';
        }
      }
    });
  }
}
