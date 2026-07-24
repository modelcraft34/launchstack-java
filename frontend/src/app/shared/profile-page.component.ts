import { AsyncPipe, NgIf } from '@angular/common';
import { Component } from '@angular/core';

import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [AsyncPipe, NgIf],
  template: `
    <section class="page-card">
      <h1>Profile</h1>
      <p *ngIf="(authService.currentUser$ | async) as user; else profilePlaceholder">
        Signed in as <strong>{{ user.firstName }} {{ user.lastName }}</strong> ({{ user.email }})
      </p>
      <ng-template #profilePlaceholder>
        <p>Profile management UI will be expanded in a future sprint.</p>
      </ng-template>
    </section>
  `
})
export class ProfilePageComponent {
  constructor(public readonly authService: AuthService) {}
}
