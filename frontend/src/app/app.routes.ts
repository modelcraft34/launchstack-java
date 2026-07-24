import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { authRedirectGuard } from './core/guards/auth-redirect.guard';
import { ForgotPasswordPageComponent } from './features/auth/forgot-password-page.component';
import { LoginPageComponent } from './features/auth/login-page.component';
import { RegisterPageComponent } from './features/auth/register-page.component';
import { ResendVerificationPageComponent } from './features/auth/resend-verification-page.component';
import { ResetPasswordPageComponent } from './features/auth/reset-password-page.component';
import { VerifyEmailPageComponent } from './features/auth/verify-email-page.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page.component';
import { UserDetailPageComponent } from './features/users/user-detail-page.component';
import { UsersPageComponent } from './features/users/users-page.component';
import { DashboardLayoutComponent } from './layout/dashboard-layout/dashboard-layout.component';
import { ProfilePageComponent } from './shared/profile-page.component';

export const routes: Routes = [
  {
    path: 'auth',
    children: [
      { path: 'login', component: LoginPageComponent, canActivate: [authRedirectGuard] },
      { path: 'register', component: RegisterPageComponent, canActivate: [authRedirectGuard] },
      { path: 'forgot-password', component: ForgotPasswordPageComponent },
      { path: 'reset-password', component: ResetPasswordPageComponent },
      { path: 'verify-email', component: VerifyEmailPageComponent },
      { path: 'resend-verification', component: ResendVerificationPageComponent },
      { path: '', pathMatch: 'full', redirectTo: 'login' }
    ]
  },
  {
    path: '',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardPageComponent },
      { path: 'users', component: UsersPageComponent },
      { path: 'users/:id', component: UserDetailPageComponent },
      { path: 'profile', component: ProfilePageComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
