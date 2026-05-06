import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { ApplicationService, LoanApplicationRequest } from '../../../core/services/application.service';

@Component({
  selector: 'app-apply',
  standalone: true,
  imports: [FormsModule, RouterLink, NavbarComponent],
  templateUrl: './apply.html',
  styleUrl: './apply.css'
})
export class ApplyComponent implements OnInit {
  // Current wizard step (0-3)
  currentStep = 0;
  steps = ['Personal', 'Employment', 'Loan Details', 'Review'];

  form: LoanApplicationRequest = {
    fullName: '', phone: '', address: '', dateOfBirth: '',
    employmentType: '', employerName: '', monthlyIncome: 0,
    loanAmount: 0, tenureMonths: 0, loanPurpose: ''
  };

  isEdit = false;
  applicationId: number | null = null;
  loading = false;
  error = '';
  success = '';

  constructor(
    private appService: ApplicationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.applicationId = +params['id'];
        this.appService.getStatus(this.applicationId).subscribe({
          next: (data) => {
            this.form = {
              fullName: data.fullName, phone: data.phone,
              address: data.address,
              dateOfBirth: data.dateOfBirth?.split('T')[0] || '',
              employmentType: data.employmentType,
              employerName: data.employerName,
              monthlyIncome: data.monthlyIncome,
              loanAmount: data.loanAmount,
              tenureMonths: data.tenureMonths,
              loanPurpose: data.loanPurpose
            };
          }
        });
      }
    });
  }

  // Move to next step — saves draft automatically on step 2 (after loan details)
  next(): void {
    if (this.currentStep < this.steps.length - 1) {
      this.currentStep++;
    }
  }

  prev(): void {
    if (this.currentStep > 0) this.currentStep--;
  }

  goToStep(i: number): void {
    if (i < this.currentStep) this.currentStep = i;
  }

  // Save as draft
  saveDraft(): void {
    this.error = '';
    this.success = '';
    this.loading = true;

    const call = this.isEdit && this.applicationId
      ? this.appService.update(this.applicationId, this.form)
      : this.appService.create(this.form);

    call.subscribe({
      next: (data) => {
        this.loading = false;
        this.applicationId = data.id;
        this.isEdit = true;
        this.success = 'Draft saved!';
        // Move to review step
        this.currentStep = 3;
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.error = 'Cannot connect to server.';
        } else if (err.error?.errors?.length) {
          this.error = err.error.errors.map((e: any) => e.defaultMessage).join(', ');
        } else {
          this.error = err.error?.message || JSON.stringify(err.error) || 'Failed to save';
        }
      }
    });
  }

  // Submit application
  submitApplication(): void {
    if (!this.applicationId) {
      // Save first then submit
      this.saveDraftThenSubmit();
      return;
    }
    this.loading = true;
    this.appService.submit(this.applicationId).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Application submitted successfully!';
        setTimeout(() => this.router.navigate(['/user/dashboard']), 1500);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to submit';
      }
    });
  }

  private saveDraftThenSubmit(): void {
    this.loading = true;
    const call = this.appService.create(this.form);
    call.subscribe({
      next: (data) => {
        this.applicationId = data.id;
        this.appService.submit(data.id).subscribe({
          next: () => {
            this.loading = false;
            this.success = 'Application submitted!';
            setTimeout(() => this.router.navigate(['/user/dashboard']), 1500);
          },
          error: () => { this.loading = false; this.error = 'Failed to submit'; }
        });
      },
      error: () => { this.loading = false; this.error = 'Failed to save'; }
    });
  }
}
