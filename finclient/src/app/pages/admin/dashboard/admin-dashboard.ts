import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe, NavbarComponent],
  templateUrl: './admin-dashboard.html'
})
export class AdminDashboardComponent implements OnInit {
  reports: any = null;
  applications: any[] = [];
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getReports().subscribe({
      next: (data) => { this.reports = data; },
      error: () => {}
    });

    this.adminService.getAllApplications().subscribe({
      next: (data) => { this.applications = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  getBadgeClass(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'badge-draft', SUBMITTED: 'badge-submitted',
      DOCS_PENDING: 'badge-pending', DOCS_VERIFIED: 'badge-verified',
      UNDER_REVIEW: 'badge-review', APPROVED: 'badge-approved', REJECTED: 'badge-rejected'
    };
    return map[status] || 'badge-draft';
  }
}
