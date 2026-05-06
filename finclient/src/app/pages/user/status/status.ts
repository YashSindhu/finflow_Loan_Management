import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { ApplicationService } from '../../../core/services/application.service';

// All possible statuses in order
const STATUS_FLOW = [
  { key: 'DRAFT',        label: 'Draft Created',         desc: 'Application saved as draft' },
  { key: 'SUBMITTED',    label: 'Application Submitted',  desc: 'Submitted for review' },
  { key: 'DOCS_PENDING', label: 'Documents Pending',      desc: 'Please upload required documents' },
  { key: 'DOCS_VERIFIED',label: 'Documents Verified',     desc: 'All documents verified successfully' },
  { key: 'UNDER_REVIEW', label: 'Under Review',           desc: 'Application is being reviewed by admin' },
  { key: 'APPROVED',     label: 'Approved',               desc: 'Congratulations! Your loan is approved' },
  { key: 'REJECTED',     label: 'Rejected',               desc: 'Application was not approved' },
];

@Component({
  selector: 'app-status',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, NavbarComponent],
  templateUrl: './status.html',
  styleUrl: './status.css'
})
export class StatusComponent implements OnInit {
  application: any = null;
  loading = true;
  error = '';
  timeline: any[] = [];

  constructor(private route: ActivatedRoute, private appService: ApplicationService) {}

  ngOnInit(): void {
    const id = +this.route.snapshot.paramMap.get('id')!;
    this.appService.getStatus(id).subscribe({
      next: (data) => {
        this.application = data;
        this.loading = false;
        this.buildTimeline(data.status);
      },
      error: () => { this.loading = false; this.error = 'Failed to load application'; }
    });
  }

  buildTimeline(currentStatus: string): void {
    // For APPROVED/REJECTED we show up to that status
    const currentIndex = STATUS_FLOW.findIndex(s => s.key === currentStatus);
    // If rejected, don't show APPROVED and vice versa
    let flow = STATUS_FLOW.filter(s => {
      if (currentStatus === 'APPROVED' && s.key === 'REJECTED') return false;
      if (currentStatus === 'REJECTED' && s.key === 'APPROVED') return false;
      return true;
    });

    const adjustedIndex = flow.findIndex(s => s.key === currentStatus);

    this.timeline = flow.map((s, i) => ({
      ...s,
      state: i < adjustedIndex ? 'done' : i === adjustedIndex ? 'active' : 'pending'
    }));
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
