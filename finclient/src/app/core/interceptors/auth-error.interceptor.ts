import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

// This interceptor watches every HTTP response.
// If the server returns 401 (Unauthorized), it means the JWT token
// has expired or is invalid — so we log the user out automatically.
export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401 && auth.isLoggedIn()) {
        // Token expired — clear session and redirect to login
        auth.logout();
        router.navigate(['/login'], {
          queryParams: { expired: 'true' }
        });
      }
      return throwError(() => error);
    })
  );
};
