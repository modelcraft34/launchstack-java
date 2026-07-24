import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { TokenStorageService } from '../services/token-storage.service';

export const authGuard: CanActivateFn = () => {
  const tokenStorage = inject(TokenStorageService);
  if (tokenStorage.hasAccessToken()) {
    return true;
  }

  return inject(Router).createUrlTree(['/auth/login']);
};
