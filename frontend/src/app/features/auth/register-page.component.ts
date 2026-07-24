import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-page">
      <h1>Create account</h1>
      <p class="subtitle">Register and verify your email before your first login.</p>

      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        <label>
          First name
          <input type="text" formControlName="firstName" autocomplete="given-name" />
        </label>
        <p class="field-error" *ngIf="isInvalid('firstName')">First name is required.</p>

        <label>
          Last name
          <input type="text" formControlName="lastName" autocomplete="family-name" />
        </label>
        <p class="field-error" *ngIf="isInvalid('lastName')">Last name is required.</p>

        <label>
          Email
          <input type="email" formControlName="email" autocomplete="email" />
        </label>
        <p class="field-error" *ngIf="isInvalid('email')">Enter a valid email address.</p>

        <label>
          Password
          <input type="password" formControlName="password" autocomplete="new-password" />
        </label>
        <p class="field-error" *ngIf="isInvalid('password')">Password must be at least 8 characters.</p>

        <p class="form-success" *ngIf="successMessage">{{ successMessage }}</p>
        <p class="form-error" *ngIf="errorMessage">{{ errorMessage }}</p>

        <button type="submit" [disabled]="loading || form.invalid">
          {{ loading ? 'Creating account...' : 'Register' }}
        </button>
      </form>

      <nav class="auth-links">
        <a routerLink="/auth/login">Back to login</a>
        <a routerLink="/auth/resend-verification">Need verification email?</a>
      </nav>
    </section>
  `
})
export class RegisterPageComponent {
  private readonly formBuilder = inject(FormBuilder);

  loading = false;
  successMessage = '';
  errorMessage = '';

  readonly form = this.formBuilder.nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(private readonly authService: AuthService) {}

  submit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.register(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.loading = false;
        this.form.reset();
        this.successMessage = response.message || 'Registration completed. Please verify your email.';
      },
      error: (error: unknown) => {
        this.loading = false;
        this.errorMessage = this.toErrorMessage(error, 'Registration failed. Please try again.');
      }
    });
  }

  isInvalid(fieldName: 'firstName' | 'lastName' | 'email' | 'password'): boolean {
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
