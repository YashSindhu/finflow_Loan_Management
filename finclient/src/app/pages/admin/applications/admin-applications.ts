import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-admin-applications',
  standalone: true,
  imports: [RouterLink, DecimalPipe, FormsModule, NavbarComponent],
  templateUrl: './admin-applications.html'
})
export class AdminApplicationsComponent implements OnInit {
  applications: any[] = [];
  loading = true;
  searchText = '';
  filterStatus = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getAllApplications().subscribe({
      next: (data) => { this.applications = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get filtered(): any[] {
    return this.applications.filter(app => {
      const matchesSearch = !this.searchText ||
        (app.email || '').toLowerCase().includes(this.searchText.toLowerCase()) ||
        (app.fullName || '').toLowerCase().includes(this.searchText.toLowerCase());
      const matchesStatus = !this.filterStatus || app.status === this.filterStatus;
      return matchesSearch && matchesStatus;
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
