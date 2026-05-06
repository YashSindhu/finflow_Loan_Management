import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API = '/gateway';

export interface DecisionRequest {
  decisionType: 'APPROVED' | 'REJECTED';
  remarks: string;
  approvedAmount?: number;
  interestRate?: number;
  tenureMonths?: number;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private http: HttpClient) {}

  getAllApplications(): Observable<any[]> {
    return this.http.get<any[]>(`${API}/admin/applications`);
  }

  getApplication(id: number): Observable<any> {
    return this.http.get(`${API}/admin/applications/${id}`);
  }

  makeDecision(id: number, data: DecisionRequest): Observable<any> {
    return this.http.post(`${API}/admin/applications/${id}/decision`, data);
  }

  getDecision(id: number): Observable<any> {
    return this.http.get(`${API}/admin/applications/${id}/decision`);
  }

  getReports(): Observable<any> {
    return this.http.get(`${API}/admin/reports`);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${API}/admin/users`);
  }

  getProducts(): Observable<any[]> {
    return this.http.get<any[]>(`${API}/products`);
  }

  viewDocument(docId: number): Observable<Blob> {
    return this.http.get(`${API}/documents/admin/${docId}/view`, { responseType: 'blob' });
  }
}
