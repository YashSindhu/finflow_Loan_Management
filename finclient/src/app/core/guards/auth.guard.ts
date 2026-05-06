import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Checks if token is expired by reading the JWT payload
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  } catch {
    return true; // If we can't parse it, treat as expired
  }
}

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.getToken();

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  // Auto-logout if token is expired
  if (isTokenExpired(token)) {
    auth.logout(); // clears localStorage automatically
    router.navigate(['/login']);
    return false;
  }

  return true;
};
