import { AsyncPipe, NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [AsyncPipe, NgIf, RouterLink],
  template: `
    <nav class="top-nav">
      <a routerLink="/dashboard" class="brand">LaunchStack Java</a>

      <div class="nav-actions">
        <span *ngIf="(authService.currentUser$ | async) as user">{{ user.firstName }} {{ user.lastName }}</span>
        <button type="button" (click)="logout()">Logout</button>
      </div>
    </nav>
  `
})
export class NavbarComponent {
  constructor(
    public readonly authService: AuthService,
    private readonly router: Router
  ) {}

  logout(): void {
    this.authService.logout().subscribe(() => {
      void this.router.navigate(['/auth/login']);
    });
  }
}
