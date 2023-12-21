import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';

@Injectable()
export class HasKeyParamGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const key = route.queryParams?.key;

    if (!key) {
      this.router.navigate(['/login']);
    }

    return true;
  }
}
