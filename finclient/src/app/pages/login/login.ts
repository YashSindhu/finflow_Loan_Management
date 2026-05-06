import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html'
})
export class LoginComponent {
  form = { email: '', password: '' };
  loading = false;
  error = '';
  showPassword = false;
  expiredMessage = '';

  constructor(private auth: AuthService, private router: Router, private route: ActivatedRoute) {
    // Clear any expired token on login page load
    if (auth.isLoggedIn()) {
      auth.logout();
    }
    // Show message if redirected due to session expiry
    this.route.queryParams.subscribe(params => {
      if (params['expired']) {
        this.expiredMessage = 'Your session has expired. Please login again.';
      }
    });
  }

  onSubmit(): void {
    this.error = '';
    this.loading = true;

    this.auth.login(this.form).subscribe({
      next: () => {
        this.loading = false;
        // Redirect based on role
        if (this.auth.isAdmin()) {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.router.navigate(['/user/dashboard']);
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Invalid email or password';
      }
    });
  }
}
