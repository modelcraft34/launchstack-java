import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TokenStorageService } from '../services/token-storage.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const accessToken = tokenStorage.getAccessToken();
  const isApiRequest = req.url.startsWith(environment.apiBaseUrl) || req.url.startsWith('/api/');

  const request = accessToken && isApiRequest
    ? req.clone({
        setHeaders: {
          Authorization: 'Bearer ' + accessToken
        }
      })
    : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && isApiRequest) {
        tokenStorage.clearTokens();
      }
      return throwError(() => error);
    })
  );
};
