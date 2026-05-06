import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { ApplicationService } from '../../../core/services/application.service';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, NavbarComponent],
  templateUrl: './dashboard.html'
})
export class UserDashboardComponent implements OnInit {
  applications: any[] = [];
  loading = true;

  constructor(private appService: ApplicationService) {}

  ngOnInit(): void {
    this.appService.getMyApplications().subscribe({
      next: (data) => { this.applications = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  getBadgeClass(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'badge-draft',
      SUBMITTED: 'badge-submitted',
      DOCS_PENDING: 'badge-pending',
      DOCS_VERIFIED: 'badge-verified',
      UNDER_REVIEW: 'badge-review',
      APPROVED: 'badge-approved',
      REJECTED: 'badge-rejected'
    };
    return map[status] || 'badge-draft';
  }
}
