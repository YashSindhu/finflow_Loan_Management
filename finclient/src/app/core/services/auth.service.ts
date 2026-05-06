import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

const API = '/gateway';

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  register(data: RegisterRequest): Observable<string> {
    return this.http.post(`${API}/auth/register`, data, { responseType: 'text' });
  }

  login(data: LoginRequest): Observable<any> {
    return this.http.post(`${API}/auth/login`, data, { observe: 'response' }).pipe(
      tap((res: any) => {
        const token = res.headers.get('Authorization')?.replace('Bearer ', '');
        if (token) localStorage.setItem('token', token);
        if (res.body?.role) localStorage.setItem('role', res.body.role);
        if (res.body?.email) localStorage.setItem('email', res.body.email);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
  }

  getToken(): string | null { return localStorage.getItem('token'); }
  getRole(): string | null  { return localStorage.getItem('role'); }
  getEmail(): string | null { return localStorage.getItem('email'); }
  isLoggedIn(): boolean     { return !!this.getToken(); }
  isAdmin(): boolean        { return this.getRole() === 'ROLE_ADMIN' || this.getRole() === 'ROLE_SUPER_ADMIN'; }
  isSuperAdmin(): boolean   { return this.getRole() === 'ROLE_SUPER_ADMIN'; }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${API}/auth/admin/users`);
  }

  updateUserRole(id: number, role: string): Observable<any> {
    return this.http.put(`${API}/auth/admin/users/${id}`, { role });
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${API}/auth/admin/users/${id}`, { responseType: 'text' });
  }

  getProfile(): Observable<any> {
    return this.http.get(`${API}/auth/profile`);
  }

  updateProfile(name: string, email: string): Observable<any> {
    return this.http.put(`${API}/auth/profile`, { name, email });
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${API}/auth/profile/password`, { oldPassword, newPassword }, { responseType: 'text' });
  }
}
