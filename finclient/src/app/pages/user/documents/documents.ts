import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { DocumentService } from '../../../core/services/document.service';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [FormsModule, DatePipe, NavbarComponent],
  templateUrl: './documents.html'
})
export class DocumentsComponent implements OnInit {
  documents: any[] = [];
  loading = true;
  uploading = false;
  error = '';
  success = '';

  uploadForm = { applicationId: null as number | null, documentType: '' };
  selectedFile: File | null = null;

  constructor(private docService: DocumentService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    // Pre-fill applicationId from query param if coming from dashboard
    this.route.queryParams.subscribe(params => {
      if (params['appId']) this.uploadForm.applicationId = +params['appId'];
    });
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.loading = true;
    this.docService.getMyDocuments().subscribe({
      next: (data) => {
        console.log('My documents:', data);
        this.documents = [...data];
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load documents:', err);
        this.loading = false;
      }
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.selectedFile = input.files[0];
  }

  upload(): void {
    if (!this.uploadForm.applicationId || !this.uploadForm.documentType || !this.selectedFile) {
      this.error = 'Please fill all fields and select a file';
      return;
    }
    this.error = '';
    this.success = '';
    this.uploading = true;

    this.docService.upload(this.uploadForm.applicationId, this.uploadForm.documentType, this.selectedFile).subscribe({
      next: () => {
        this.uploading = false;
        this.success = 'Document uploaded successfully!';
        this.loadDocuments();
      },
      error: (err) => {
        this.uploading = false;
        this.error = err.error?.message || 'Upload failed';
      }
    });
  }

  viewDoc(docId: number): void {
    this.error = '';
    this.docService.viewDocument(docId).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
      },
      error: () => { this.error = 'Failed to load document'; }
    });
  }

  getDocBadge(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'badge-pending',
      VERIFIED: 'badge-verified',
      REJECTED: 'badge-rejected'
    };
    return map[status] || 'badge-draft';
  }

  // Re-upload a rejected document
  reUpload(event: Event, applicationId: number, documentType: string): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.error = '';
    this.success = '';
    this.docService.upload(applicationId, documentType, input.files[0]).subscribe({
      next: () => { this.success = 'Document re-uploaded successfully!'; this.loadDocuments(); },
      error: () => { this.error = 'Re-upload failed'; }
    });
  }
}
