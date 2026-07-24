import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { TokenStorageService } from '../services/token-storage.service';

export const authRedirectGuard: CanActivateFn = () => {
  const tokenStorage = inject(TokenStorageService);
  if (tokenStorage.hasAccessToken()) {
    return inject(Router).createUrlTree(['/dashboard']);
  }

  return true;
};
