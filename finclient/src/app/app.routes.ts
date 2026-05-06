import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  // Default redirect
  { path: '', redirectTo: '/home', pathMatch: 'full' },

  // Landing page (public)
  {
    path: 'home',
    loadComponent: () => import('./pages/landing/landing').then(m => m.LandingComponent)
  },

  // Public pages
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then(m => m.RegisterComponent)
  },

  // User pages (requires login)
  {
    path: 'user/dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/user/dashboard/dashboard').then(m => m.UserDashboardComponent)
  },
  {
    path: 'user/apply',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/user/apply/apply').then(m => m.ApplyComponent)
  },
  {
    path: 'user/documents',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/user/documents/documents').then(m => m.DocumentsComponent)
  },
  {
    path: 'user/profile',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/user/profile/profile').then(m => m.ProfileComponent)
  },
  {
    path: 'user/status/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/user/status/status').then(m => m.StatusComponent)
  },

  // Admin pages (requires login + admin role)
  {
    path: 'admin/dashboard',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./pages/admin/dashboard/admin-dashboard').then(m => m.AdminDashboardComponent)
  },
  {
    path: 'admin/applications',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./pages/admin/applications/admin-applications').then(m => m.AdminApplicationsComponent)
  },
  {
    path: 'admin/applications/:id',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./pages/admin/application-detail/application-detail').then(m => m.ApplicationDetailComponent)
  },
  {
    path: 'admin/users',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./pages/admin/users/admin-users').then(m => m.AdminUsersComponent)
  },

  // Catch-all
  { path: '**', redirectTo: '/login' }
];
