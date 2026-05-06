import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API = '/gateway';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  constructor(private http: HttpClient) {}

  upload(applicationId: number, documentType: string, file: File): Observable<any> {
    const form = new FormData();
    form.append('applicationId', applicationId.toString());
    form.append('documentType', documentType);
    form.append('file', file);
    return this.http.post(`${API}/documents/upload`, form);
  }

  getMyDocuments(): Observable<any[]> {
    return this.http.get<any[]>(`${API}/documents/my`);
  }

  getByApplication(applicationId: number): Observable<any[]> {
    return this.http.get<any[]>(`${API}/documents/application/${applicationId}`);
  }

  // Admin
  getAllDocuments(): Observable<any[]> {
    return this.http.get<any[]>(`${API}/documents/admin/all`);
  }

  verify(id: number, status: string, remarks: string): Observable<any> {
    return this.http.put(`${API}/documents/admin/${id}/verify`, { status, remarks });
  }

  viewDocument(docId: number): Observable<Blob> {
    return this.http.get(`${API}/documents/${docId}/view`, { responseType: 'blob' });
  }
}
