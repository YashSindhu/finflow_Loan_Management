import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API = '/gateway';

export interface LoanApplicationRequest {
  fullName: string;
  phone: string;
  address: string;
  dateOfBirth: string;
  employmentType: string;
  employerName: string;
  monthlyIncome: number;
  loanAmount: number;
  tenureMonths: number;
  loanPurpose: string;  // HOME | PERSONAL | BUSINESS | EDUCATION | VEHICLE | OTHER
}

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  constructor(private http: HttpClient) {}

  create(data: LoanApplicationRequest): Observable<any> {
    return this.http.post(`${API}/applications`, data);
  }

  update(id: number, data: LoanApplicationRequest): Observable<any> {
    return this.http.put(`${API}/applications/${id}`, data);
  }

  submit(id: number): Observable<any> {
    return this.http.post(`${API}/applications/${id}/submit`, {});
  }

  getMyApplications(): Observable<any[]> {
    return this.http.get<any[]>(`${API}/applications/my`);
  }

  getStatus(id: number): Observable<any> {
    return this.http.get(`${API}/applications/${id}/status`);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${API}/applications/admin/${id}/status`, { status });
  }

  // Admin
  getAllApplications(): Observable<any[]> {
    return this.http.get<any[]>(`${API}/applications/admin/all`);
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${API}/applications/admin/${id}`);
  }
}
