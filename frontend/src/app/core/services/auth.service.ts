import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, of, tap, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { AuthTokenResponse } from '../models/auth-token-response.model';
import { CurrentUser } from '../models/current-user.model';
import { TokenStorageService } from './token-storage.service';

interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiBaseUrl = environment.apiBaseUrl;
  private readonly currentUserSubject = new BehaviorSubject<CurrentUser | null>(null);

  readonly currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private readonly http: HttpClient,
    private readonly tokenStorage: TokenStorageService
  ) {
    if (this.tokenStorage.hasAccessToken()) {
      this.getCurrentUser().subscribe({
        error: () => {
          this.tokenStorage.clearTokens();
          this.currentUserSubject.next(null);
        }
      });
    }
  }

  isAuthenticated(): boolean {
    return this.tokenStorage.hasAccessToken();
  }

  login(request: LoginRequest): Observable<AuthTokenResponse> {
    return this.http.post<ApiResponse<AuthTokenResponse>>(this.endpoint('/api/auth/login'), request).pipe(
      map((response) => this.requireData(response)),
      tap((tokens) => {
        this.tokenStorage.setAccessToken(tokens.accessToken);
        this.tokenStorage.setRefreshToken(tokens.refreshToken);
        this.getCurrentUser().subscribe({
          error: () => this.currentUserSubject.next(null)
        });
      })
    );
  }

  register(request: RegisterRequest): Observable<ApiResponse<null>> {
    return this.http.post<ApiResponse<null>>(this.endpoint('/api/auth/register'), request);
  }

  refresh(refreshToken?: string): Observable<AuthTokenResponse> {
    const token = refreshToken ?? this.tokenStorage.getRefreshToken();
    if (!token) {
      return throwError(() => new Error('No refresh token found.'));
    }

    return this.http
      .post<ApiResponse<AuthTokenResponse>>(this.endpoint('/api/auth/refresh'), { refreshToken: token })
      .pipe(
        map((response) => this.requireData(response)),
        tap((tokens) => {
          this.tokenStorage.setAccessToken(tokens.accessToken);
          this.tokenStorage.setRefreshToken(tokens.refreshToken);
        })
      );
  }

  logout(): Observable<void> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (!refreshToken) {
      this.clearSession();
      return of(void 0);
    }

    return this.http.post<ApiResponse<null>>(this.endpoint('/api/auth/logout'), { refreshToken }).pipe(
      map(() => void 0),
      catchError(() => of(void 0)),
      tap(() => this.clearSession())
    );
  }

  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<ApiResponse<CurrentUser>>(this.endpoint('/api/auth/me')).pipe(
      map((response) => this.requireData(response)),
      tap((user) => this.currentUserSubject.next(user))
    );
  }

  me(): Observable<CurrentUser> {
    return this.getCurrentUser();
  }

  verifyEmail(token: string): Observable<ApiResponse<null>> {
    return this.http.post<ApiResponse<null>>(this.endpoint('/api/auth/verify-email'), { token });
  }

  resendVerification(email: string): Observable<ApiResponse<null>> {
    return this.http.post<ApiResponse<null>>(this.endpoint('/api/auth/resend-verification'), { email });
  }

  forgotPassword(email: string): Observable<ApiResponse<null>> {
    return this.http.post<ApiResponse<null>>(this.endpoint('/api/auth/forgot-password'), { email });
  }

  resetPassword(token: string, newPassword: string): Observable<ApiResponse<null>> {
    return this.http.post<ApiResponse<null>>(this.endpoint('/api/auth/reset-password'), {
      token,
      newPassword
    });
  }

  private endpoint(path: string): string {
    return `${this.apiBaseUrl}${path}`;
  }

  private requireData<T>(response: ApiResponse<T>): T {
    if (response.data === null) {
      throw new Error(response.message || 'Response does not include data.');
    }

    return response.data;
  }

  private clearSession(): void {
    this.tokenStorage.clearTokens();
    this.currentUserSubject.next(null);
  }
}
