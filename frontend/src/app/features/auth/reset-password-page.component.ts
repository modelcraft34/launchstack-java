import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (!password || !confirmPassword) {
    return null;
  }

  return password === confirmPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-reset-password-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-page">
      <h1>Reset password</h1>
      <p class="subtitle">Set a new password for your account.</p>

      <p class="form-error" *ngIf="missingToken">Reset token is missing. Please use the link from your email.</p>

      <form [formGroup]="form" (ngSubmit)="submit()" novalidate *ngIf="!missingToken">
        <label>
          New password
          <input type="password" formControlName="newPassword" autocomplete="new-password" />
        </label>
        <p class="field-error" *ngIf="isInvalid('newPassword')">Password must be at least 8 characters.</p>

        <label>
          Confirm password
          <input type="password" formControlName="confirmPassword" autocomplete="new-password" />
        </label>
        <p class="field-error" *ngIf="isInvalid('confirmPassword')">Confirm password is required.</p>
        <p
          class="field-error"
          *ngIf="form.errors?.['passwordMismatch'] && (form.controls.newPassword.touched || form.controls.confirmPassword.touched)">
          Passwords do not match.
        </p>

        <p class="form-success" *ngIf="successMessage">{{ successMessage }}</p>
        <p class="form-error" *ngIf="errorMessage">{{ errorMessage }}</p>

        <button type="submit" [disabled]="loading || form.invalid">
          {{ loading ? 'Resetting...' : 'Reset password' }}
        </button>
      </form>

      <a routerLink="/auth/login" *ngIf="successMessage || missingToken">Back to login</a>
    </section>
  `
})
export class ResetPasswordPageComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);

  loading = false;
  missingToken = false;
  successMessage = '';
  errorMessage = '';
  private token: string | null = null;

  readonly form = this.formBuilder.nonNullable.group(
    {
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: passwordMatchValidator }
  );

  constructor(
    private readonly route: ActivatedRoute,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');
    this.missingToken = !this.token;
  }

  submit(): void {
    if (this.form.invalid || this.loading || !this.token) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';
    const { newPassword } = this.form.getRawValue();

    this.authService.resetPassword(this.token, newPassword).subscribe({
      next: (response) => {
        this.loading = false;
        this.form.reset();
        this.successMessage = response.message || 'Password reset successfully.';
      },
      error: (error: unknown) => {
        this.loading = false;
        this.errorMessage = this.toErrorMessage(error, 'Password reset failed.');
      }
    });
  }

  isInvalid(fieldName: 'newPassword' | 'confirmPassword'): boolean {
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
