import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  styles: [`
    aside {
      width: 220px;
      padding: 1rem;
      background: #ffffff;
      border-right: 1px solid #e5e7eb;
    }

    nav {
      display: grid;
      gap: 0.5rem;
    }

    a {
      padding: 0.75rem 1rem;
      border-radius: 0.5rem;
      color: #374151;
      text-decoration: none;
    }

    a.active {
      background: #e0f2fe;
      color: #0f172a;
      font-weight: 600;
    }
  `],
  template: `
    <aside>
      <nav>
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
        <a routerLink="/users" routerLinkActive="active">Users</a>
        <a routerLink="/auth" routerLinkActive="active">Auth</a>
      </nav>
    </aside>
  `
})
export class SidebarComponent {}
