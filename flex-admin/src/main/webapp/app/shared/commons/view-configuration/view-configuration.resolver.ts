import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { Screen } from '@app/shared/enums';

import { ViewConfigurationService } from './view-configuration.service';

@Injectable()
export class ViewConfigurationResolver implements Resolve<boolean> {
  constructor(private service: ViewConfigurationService) {}

  resolve(route: ActivatedRouteSnapshot): any {
    const screen = route.data.screen as Screen;

    return this.service.getConfiguration(screen);
  }
}
