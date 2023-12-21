import { Injectable } from '@angular/core';
import { RouterStateSnapshot } from '@angular/router';
import { RouterStateSerializer } from '@ngrx/router-store';

import { RouterStateUrl } from './router.state';

/**
 * Serializer for router state
 */
@Injectable()
export class RouterSerializer implements RouterStateSerializer<RouterStateUrl> {
  /**
   * Serialize route data from RouterStateSnapshot
   *
   * @param routerState current rooute state
   */
  serialize(routerState: RouterStateSnapshot): RouterStateUrl {
    let route = routerState.root;

    while (route.firstChild) {
      route = route.firstChild;
    }

    const {
      url,
      root: { queryParams },
    } = routerState;
    const { params } = route;

    return { url, params, queryParams };
  }
}
