import { Observable } from 'rxjs';

import { Injectable } from '@angular/core';
import { Resolve } from '@angular/router';
import { FspUserRegistrationDTO } from '@app/shared/models';

import { RegistrationThreadService } from './registration-thread.service';

@Injectable()
export class RegistrationThreadResolver implements Resolve<FspUserRegistrationDTO> {
  constructor(private service: RegistrationThreadService) {}

  resolve(): Observable<FspUserRegistrationDTO> {
    return this.service.getFspRegistration();
  }
}
