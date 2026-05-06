import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { AdminService, DecisionRequest } from '../../../core/services/admin.service';
import { DocumentService } from '../../../core/services/document.service';
import { ApplicationService } from '../../../core/services/application.service';

@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe, DecimalPipe, NavbarComponent],
  templateUrl: './application-detail.html'
})
export class ApplicationDetailComponent implements OnInit {
  applicationId!: number;
  application: any = null;
  documents: any[] = [];
  existingDecision: any = null;
  loading = true;
  deciding = false;
  error = '';
  success = '';

  decisionForm: DecisionRequest = {
    decisionType: 'APPROVED', remarks: '', approvedAmount: 0, interestRate: 0, tenureMonths: 0
  };

  constructor(
    private route: ActivatedRoute,
    private adminService: AdminService,
    private docService: DocumentService,
    private appService: ApplicationService
  ) {}

  ngOnInit(): void {
    this.applicationId = +this.route.snapshot.paramMap.get('id')!;
    this.loadData();
  }

  loadData(): void {
    this.adminService.getApplication(this.applicationId).subscribe({
      next: (data) => {
        this.application = data;
        this.loading = false;
        this.decisionForm.approvedAmount = data.loanAmount;
        this.decisionForm.tenureMonths = data.tenureMonths;
      },
      error: () => { this.loading = false; this.error = 'Failed to load application'; }
    });

    this.docService.getByApplication(this.applicationId).subscribe({
      next: (docs) => { this.documents = docs; },
      error: () => {}
    });

    this.adminService.getDecision(this.applicationId).subscribe({
      next: (d) => { this.existingDecision = d; },
      error: () => { this.existingDecision = null; } // No decision yet, that's fine
    });
  }

  verifyDoc(docId: number, status: string, remarks: string): void {
    this.docService.verify(docId, status, remarks).subscribe({
      next: () => {
        this.success = `Document ${status.toLowerCase()}`;
        if (status === 'VERIFIED') {
          // Check if all docs will be verified after this
          const remainingPending = this.documents.filter(d => d.id !== docId && d.status === 'PENDING').length;
          if (remainingPending === 0) {
            // All docs verified — move to UNDER_REVIEW
            this.appService.updateStatus(this.applicationId, 'UNDER_REVIEW').subscribe({
              next: () => { this.loadData(); },
              error: () => { this.loadData(); }
            });
          } else {
            this.appService.updateStatus(this.applicationId, 'DOCS_VERIFIED').subscribe({
              next: () => { this.loadData(); },
              error: () => { this.loadData(); }
            });
          }
        } else {
          this.loadData();
        }
      },
      error: () => { this.error = 'Failed to update document'; }
    });
  }

  makeDecision(): void {
    this.deciding = true;
    this.error = '';

    // For REJECTED, don't send amount/rate/tenure (backend validation requires > 0)
    const payload: DecisionRequest = {
      decisionType: this.decisionForm.decisionType,
      remarks: this.decisionForm.remarks
    };
    if (this.decisionForm.decisionType === 'APPROVED') {
      payload.approvedAmount = this.decisionForm.approvedAmount;
      payload.interestRate = this.decisionForm.interestRate;
      payload.tenureMonths = this.decisionForm.tenureMonths;
    }

    this.adminService.makeDecision(this.applicationId, payload).subscribe({
      next: (d) => {
        this.deciding = false;
        this.existingDecision = d;
        this.success = 'Decision submitted successfully!';
      },
      error: (err) => {
        this.deciding = false;
        console.error('Decision error:', err);
        if (err.status === 409 || err.error?.message?.includes('already')) {
          this.error = 'Decision already exists. Refreshing...';
          this.loadData();
        } else {
          this.error = err.error?.message || err.message || 'Failed to submit decision';
        }
      }
    });
  }

  requestDocuments(): void {
    this.loading = true;
    this.appService.updateStatus(this.applicationId, 'DOCS_PENDING').subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Documents requested from applicant successfully!';
        this.loadData();
      },
      error: () => { this.loading = false; this.error = 'Failed to request documents'; }
    });
  }

  allDocsVerified(): boolean {
    const nonRejected = this.documents.filter(d => d.status !== 'REJECTED');
    return nonRejected.length > 0 && nonRejected.every(d => d.status === 'VERIFIED');
  }

  getBadgeClass(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'badge-draft', SUBMITTED: 'badge-submitted',
      DOCS_PENDING: 'badge-pending', DOCS_VERIFIED: 'badge-verified',
      UNDER_REVIEW: 'badge-review', APPROVED: 'badge-approved', REJECTED: 'badge-rejected'
    };
    return map[status] || 'badge-draft';
  }

  getDocBadge(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'badge-pending', VERIFIED: 'badge-verified', REJECTED: 'badge-rejected'
    };
    return map[status] || 'badge-draft';
  }

  viewDoc(docId: number): void {
    this.error = '';
    this.adminService.viewDocument(docId).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
      },
      error: (err) => {
        console.error('View doc error:', err);
        this.error = `Failed to load document (${err.status}: ${err.statusText})`;
      }
    });
  }
}
