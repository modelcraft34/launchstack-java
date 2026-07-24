import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-page">
      <h1>Sign in</h1>
      <p class="subtitle">Use your account to access LaunchStack.</p>

      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        <label>
          Email
          <input type="email" formControlName="email" autocomplete="email" />
        </label>
        <p class="field-error" *ngIf="isInvalid('email')">Enter a valid email address.</p>

        <label>
          Password
          <input type="password" formControlName="password" autocomplete="current-password" />
        </label>
        <p class="field-error" *ngIf="isInvalid('password')">Password must be at least 8 characters.</p>

        <p class="form-error" *ngIf="errorMessage">{{ errorMessage }}</p>

        <button type="submit" [disabled]="loading || form.invalid">
          {{ loading ? 'Signing in...' : 'Sign in' }}
        </button>
      </form>

      <nav class="auth-links">
        <a routerLink="/auth/register">Create account</a>
        <a routerLink="/auth/forgot-password">Forgot password?</a>
        <a routerLink="/auth/resend-verification">Resend verification</a>
      </nav>
    </section>
  `
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder);

  loading = false;
  errorMessage = '';

  readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigate(['/dashboard']);
      },
      error: (error: unknown) => {
        this.loading = false;
        this.errorMessage = this.toErrorMessage(error, 'Login failed. Please try again.');
      }
    });
  }

  isInvalid(fieldName: 'email' | 'password'): boolean {
    const control = this.form.controls[fieldName];
    return control.invalid && control.touched;
  }

  private toErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message ?? fallback;
    }

    return fallback;
  }
}
