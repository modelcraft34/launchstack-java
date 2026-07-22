import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { NavbarComponent } from '../navbar/navbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [NavbarComponent, RouterOutlet, SidebarComponent],
  styles: [`
    .shell {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .content {
      display: flex;
      flex: 1;
    }

    main {
      flex: 1;
      padding: 2rem;
    }
  `],
  template: `
    <div class="shell">
      <app-navbar></app-navbar>
      <div class="content">
        <app-sidebar></app-sidebar>
        <main>
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `
})
export class DashboardLayoutComponent {}
