import { Component, OnInit } from '@angular/core';
import { NavbarComponent } from '../../../shared/navbar/navbar';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [NavbarComponent],
  templateUrl: './admin-users.html'
})
export class AdminUsersComponent implements OnInit {
  users: any[] = [];
  loading = true;
  error = '';
  success = '';

  constructor(public auth: AuthService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.auth.getAllUsers().subscribe({
      next: (data) => { this.users = data; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load users'; }
    });
  }

  promoteToAdmin(id: number): void {
    this.auth.updateUserRole(id, 'ROLE_ADMIN').subscribe({
      next: () => {
        this.success = 'User promoted to admin';
        this.loadUsers();
      },
      error: () => { this.error = 'Failed to update role'; }
    });
  }

  removeUser(id: number, name: string): void {
    this.auth.deleteUser(id).subscribe({
      next: () => {
        this.success = `${name} has been removed`;
        this.users = this.users.filter(u => u.id !== id);
      },
      error: (err) => {
        this.error = err.error?.message || err.error || 'Failed to remove user';
      }
    });
  }

  getRoleLabel(role: string): string {
    const map: Record<string, string> = {
      'ROLE_USER': 'User',
      'ROLE_ADMIN': 'Admin',
      'ROLE_SUPER_ADMIN': 'Super Admin'
    };
    return map[role] || role;
  }

  getRoleBadge(role: string): string {
    const map: Record<string, string> = {
      'ROLE_USER': 'badge badge-submitted',
      'ROLE_ADMIN': 'badge badge-review',
      'ROLE_SUPER_ADMIN': 'badge badge-approved'
    };
    return map[role] || 'badge';
  }
}
