import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AdminService } from '../../core/services/admin.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html'
})
export class NavbarComponent implements OnInit {
  pendingCount = 0;

  constructor(
    public auth: AuthService,
    private router: Router,
    private adminService: AdminService,
    private themeService: ThemeService
  ) {}

  get isDark(): boolean { return this.themeService.isDark; }
  toggleTheme(): void { this.themeService.toggle(); }

  ngOnInit(): void {
    if (this.auth.isAdmin()) {
      this.adminService.getAllApplications().subscribe({
        next: (apps) => {
          this.pendingCount = apps.filter(a =>
            a.status === 'SUBMITTED' || a.status === 'DOCS_PENDING' || a.status === 'UNDER_REVIEW'
          ).length;
        },
        error: () => {}
      });
    }
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/home']);
  }
}
