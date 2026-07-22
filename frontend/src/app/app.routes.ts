import { Routes } from '@angular/router';

import { DashboardLayoutComponent } from './layout/dashboard-layout/dashboard-layout.component';
import { AuthPlaceholderComponent } from './features/auth/auth-placeholder.component';
import { DashboardPlaceholderComponent } from './features/dashboard/dashboard-placeholder.component';
import { UsersPlaceholderComponent } from './features/users/users-placeholder.component';

export const routes: Routes = [
  {
    path: '',
    component: DashboardLayoutComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardPlaceholderComponent },
      { path: 'users', component: UsersPlaceholderComponent }
    ]
  },
  { path: 'auth', component: AuthPlaceholderComponent },
  { path: '**', redirectTo: 'dashboard' }
];
