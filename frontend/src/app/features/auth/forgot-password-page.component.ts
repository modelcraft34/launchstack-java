import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-page">
      <h1>Forgot password</h1>
      <p class="subtitle">Enter your email to receive a password reset link.</p>

      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        <label>
          Email
          <input type="email" formControlName="email" autocomplete="email" />
        </label>
        <p class="field-error" *ngIf="isInvalid">Enter a valid email address.</p>

        <p class="form-success" *ngIf="successMessage">{{ successMessage }}</p>
        <p class="form-error" *ngIf="errorMessage">{{ errorMessage }}</p>

        <button type="submit" [disabled]="loading || form.invalid">
          {{ loading ? 'Sending...' : 'Send reset link' }}
        </button>
      </form>

      <a routerLink="/auth/login">Back to login</a>
    </section>
  `
})
export class ForgotPasswordPageComponent {
  private readonly formBuilder = inject(FormBuilder);

  loading = false;
  successMessage = '';
  errorMessage = '';

  readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  constructor(private readonly authService: AuthService) {}

  get isInvalid(): boolean {
    const control = this.form.controls.email;
    return control.invalid && control.touched;
  }

  submit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';
    const { email } = this.form.getRawValue();

    this.authService.forgotPassword(email).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = response.message || 'If the account exists, a reset email has been sent.';
      },
      error: (error: unknown) => {
        this.loading = false;
        this.errorMessage = this.toErrorMessage(error, 'Could not process your request.');
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
