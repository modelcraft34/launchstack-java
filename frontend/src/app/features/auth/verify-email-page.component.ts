import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-verify-email-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="auth-page">
      <h1>Email verification</h1>
      <p class="subtitle">Confirming your account...</p>

      <p class="form-success" *ngIf="successMessage">{{ successMessage }}</p>
      <p class="form-error" *ngIf="errorMessage">{{ errorMessage }}</p>

      <a routerLink="/auth/login" *ngIf="successMessage">Continue to login</a>
    </section>
  `
})
export class VerifyEmailPageComponent implements OnInit {
  successMessage = '';
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.errorMessage = 'Verification token is missing. Please use the link from your email.';
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Email verified successfully.';
      },
      error: (error: unknown) => {
        this.errorMessage = this.toErrorMessage(error, 'Email verification failed.');
      }
    });
  }

  private toErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message ?? fallback;
    }

    return fallback;
  }
}
