import * as moment from 'moment';

import { AuthService, HttpService } from '@app/core';
import { MaxOrSameControlValidator, MinOrSameControlValidator, RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { UnitDTO, UnitGeoLocationDTO } from '../unit';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { noBeforeCurrentHour, requiredIfAnyFilled, requiredIfSomeFilled } from '@app/shared/validators';

import { Dictionary } from '@app/shared/models';
import { Helpers } from '@app/shared/commons';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Role } from '@app/shared/enums';
import { UpdateValueAndValidity } from '@app/shared/commons/validators/update-validity';
import { map } from 'rxjs/operators';

@Injectable()
export class UnitsDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder, private authService: AuthService) {
    super(httpClient);
  }

  createForm(data: Partial<UnitDTO> = {}, hasRole: boolean = false): UntypedFormGroup {
    const disabledValidFrom = data.validFrom ? moment(data.validFrom).isBefore(moment()) : false;
    const disabledActive = data.validTo ? moment(data.validTo).isSameOrBefore(moment()) : false;
    const fspId = data.fspId || data.fspUser?.fspId;
    const hasSubportfolio = !!data.subportfolio;

    const geoLocations =
      data.geoLocations && data.geoLocations.length >= 1
        ? data.geoLocations
        : [{ id: null, latitude: null, longitude: null, mainLocation: false }];

    const certifiedAvailable = hasRole && data.couplingPointIdTypes?.length && data.powerStationTypes?.length && data.code?.length;

    const derTypeValidation = (key: string) =>
      requiredIfAnyFilled(
        ['hasReception', 'hasEnergyStorage', 'hasGeneration'],
        ['hasReception', 'hasEnergyStorage', 'hasGeneration'].filter((controlKey: string) => controlKey !== key)
      );

    const form = this.fb.group({
      id: [{ value: data.id, disabled: true }],
      name: [data.name, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      code: [data.code, [Validators.maxLength(50)]],
      sourcePower: [data.sourcePower, [Validators.required, MaxOrSameControlValidator('connectionPower'), UpdateValueAndValidity('pmin')]],
      connectionPower: [data.connectionPower, [Validators.required, UpdateValueAndValidity('sourcePower')]],
      directionOfDeviation: [data.directionOfDeviation, Validators.required],
      aggregated: [!!data.aggregated],
      certified: [{ value: !!data.certified, disabled: !certifiedAvailable || hasSubportfolio }],
      active: [{ value: !!data.active, disabled: disabledActive || hasSubportfolio }],
      fspId: [{ value: fspId, disabled: fspId ? true : false }, Validators.required],
      validFrom: [
        {
          value: data.validFrom ? moment(data.validFrom).toDate() : null,
          disabled: disabledValidFrom,
        },
        disabledValidFrom ? [Validators.required] : [noBeforeCurrentHour, Validators.required],
      ],
      validTo: [data.validTo ? moment(data.validTo).toDate() : null, Validators.required],
      ppe: [data.ppe, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      couplingPointIdType: [data.couplingPointIdTypes?.length ? data.couplingPointIdTypes[0] : null],
      powerStationTypes: [data.powerStationTypes?.length ? data.powerStationTypes[0] : null],
      pointOfConnectionWithLvTypes: [data.pointOfConnectionWithLvTypes?.length ? data.pointOfConnectionWithLvTypes[0] : null],
      version: [data.version],

      pmin: [data.pmin, MaxOrSameControlValidator('sourcePower')],
      qmin: [data.qmin, [MaxOrSameControlValidator('qmax'), UpdateValueAndValidity('qmax')]],
      qmax: [data.qmax, [MinOrSameControlValidator('qmin'), UpdateValueAndValidity('qmin')]],

      hasReception: [!!data.derTypeReception, derTypeValidation('hasReception')],
      hasEnergyStorage: [!!data.derTypeEnergyStorage, derTypeValidation('hasEnergyStorage')],
      hasGeneration: [!!data.derTypeGeneration, derTypeValidation('hasGeneration')],
      derTypeReception: [{ value: data.derTypeReception, disabled: !data.derTypeReception }, requiredIfSomeFilled(['hasReception'], [])],
      derTypeEnergyStorage: [
        { value: data.derTypeEnergyStorage, disabled: !data.derTypeEnergyStorage },
        requiredIfSomeFilled(['hasEnergyStorage'], []),
      ],
      derTypeGeneration: [
        { value: data.derTypeGeneration, disabled: !data.derTypeGeneration },
        requiredIfSomeFilled(['hasGeneration'], []),
      ],

      geoLocations: this.fb.array(geoLocations.map((geoLocation: UnitGeoLocationDTO) => this.createGeoLocationForm(geoLocation))),
    });

    this.authService.hasAnyRoles([Role.ROLE_ADMIN, Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR]).then((hasRole: boolean) => {
      if (!hasRole) {
        form.get('couplingPointIdType')?.disable();
        form.get('powerStationTypes')?.disable();
        form.get('pointOfConnectionWithLvTypes')?.disable();
        form.get('code')?.disable();
        form.get('pmin')?.disable();
        form.get('qmin')?.disable();
        form.get('qmax')?.disable();
      }
    });

    this.authService.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR).then((hasRole: boolean) => {
      if (hasRole) {
        form.get('powerStationTypes')?.addValidators(Validators.required);
        form.get('couplingPointIdType')?.addValidators(Validators.required);

        const controlKeys = Object.keys(form.controls);

        controlKeys
          .filter(
            (key: string) =>
              ![
                'couplingPointIdType',
                'powerStationTypes',
                'pointOfConnectionWithLvTypes',
                'certified',
                'code',
                'pmin',
                'qmin',
                'qmax',
              ].includes(key)
          )
          .forEach((key: string) => {
            form.get(key)?.disable();
          });
      }
    });

    if (data.certified) {
      Helpers.setValidatorToControls(form, ['couplingPointIdType', 'powerStationTypes', 'code'], [Validators.required]);

      this.blockCertifiedUnit(form);
    }

    return form;
  }

  createGeoLocationForm(data: Partial<UnitGeoLocationDTO> = {}): UntypedFormGroup {
    return this.fb.group({
      id: [{ value: data.id, disabled: true }],
      latitude: [data.latitude, requiredIfSomeFilled(['longitude', 'mainLocation'], ['longitude'])],
      longitude: [data.longitude, requiredIfSomeFilled(['latitude', 'mainLocation'], ['latitude'])],
      mainLocation: [!!data.mainLocation, requiredIfSomeFilled([], ['longitude', 'latitude'])],
    });
  }

  getCompanies(): Observable<Dictionary[]> {
    return this.get<{ id: number; companyName: string }[]>('flex-server/api/fsps/get-company', {
      params: {
        roles: [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
      },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }

  private blockCertifiedUnit(form: UntypedFormGroup): void {
    this.authService.hasRole(Role.ROLE_ADMIN).then((hasRole: boolean) => {
      if (!hasRole) {
        Object.keys(form.controls).forEach((controlKey: string) => form.get(controlKey)?.disable());
      }

      this.authService.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR).then((hasRole: boolean) => {
        if (hasRole) {
          form.get('certified')?.enable();
        }
      });
    });
  }
}
