import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  styles: [`
    nav {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1rem 1.5rem;
      background: #111827;
      color: #f9fafb;
    }

    a {
      color: inherit;
      text-decoration: none;
      font-weight: 600;
    }
  `],
  template: `
    <nav>
      <a routerLink="/dashboard">LaunchStack Java</a>
      <span>Sprint 0 placeholder</span>
    </nav>
  `
})
export class NavbarComponent {}
