import * as moment from 'moment';

import { AppToastrService, AuthService } from '@app/core';
import { Component, OnInit } from '@angular/core';
import { DerType, DirectionOfDeviationType, LocalizationType, Role } from '@app/shared/enums';
import { DerTypeDTO, UnitDTO, UnitGeoLocationDTO } from '../unit';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable, merge, of, catchError } from 'rxjs';
import { UntypedFormArray, UntypedFormGroup, Validators } from '@angular/forms';
import { distinctUntilChanged, map, take, takeUntil } from 'rxjs/operators';

import { DialogExtends } from '@app/shared';
import { Dictionary } from '@app/shared/models';
import { Helpers } from '@app/shared/commons';
import { HttpErrorResponse } from '@angular/common/http';
import { LocalizationTypeDTO } from '@app/content/dictionaries/dictionaries';
import { UnitsDialogService } from './dialog.service';
import { UnitsService } from '../units.service';
import { camelCase } from 'lodash-es';

interface Dictionaries {
  localizationTypesForPowerStation$: Observable<LocalizationTypeDTO[]>;
  localizationTypesForCoupling$: Observable<LocalizationTypeDTO[]>;
  pointsOfConnectionWithLv$: Observable<LocalizationTypeDTO[]>;

  directions: Dictionary[];
  types: Dictionary[];
}

@Component({
  selector: 'app-units-dialog',
  templateUrl: './dialog.component.html',
  providers: [UnitsDialogService],
})
export class UnitsDialogComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;

  currentDate = moment().set({ m: 0, s: 0, ms: 0 }).toDate();
  minDateTo = moment(this.currentDate).add(1, 'h').toDate();

  data: UnitDTO | undefined;
  companies$ = this.service.getCompanies();
  unitTypes: { [type: string]: Array<DerTypeDTO & { type: DerType }> } = {};

  localizationTypes$ = this.unitsService.getLocalizationsDict().pipe(take(1));

  dictionaries: Dictionaries = {
    directions: Helpers.enumToDictionary(DirectionOfDeviationType, 'DirectionOfDeviationType'),
    types: Helpers.enumToDictionary(DerType, 'DerType'),

    localizationTypesForCoupling$: this.localizationTypes$.pipe(
      map((response: LocalizationTypeDTO[]) => response.filter(({ type }) => type === LocalizationType.COUPLING_POINT_ID))
    ),
    localizationTypesForPowerStation$: this.localizationTypes$.pipe(
      map((response: LocalizationTypeDTO[]) => response.filter(({ type }) => type === LocalizationType.POWER_STATION_ML_LV_NUMBER))
    ),
    pointsOfConnectionWithLv$: this.localizationTypes$.pipe(
      map((response: LocalizationTypeDTO[]) => response.filter(({ type }) => type === LocalizationType.POINT_OF_CONNECTION_WITH_LV))
    ),
  };

  get certifiedAvailable(): boolean {
    const couplingPointIdValue = this.form?.get('couplingPointIdType')?.value || [];
    const powerStationValue = this.form?.get('powerStationTypes')?.value || [];
    const codeValue = this.form?.get('code')?.value || [];

    if (this.hasSubportfolio) {
      return false;
    }

    const { pmin, qmin, qmax } = this.form?.getRawValue() || {};

    if (pmin === null || qmin === null || qmax === null) {
      return false;
    }

    return couplingPointIdValue.length !== 0 && powerStationValue.length !== 0 && codeValue.length !== 0;
  }

  get certifiedUnavailableTooltipMsg(): string {
    if (this.hasSubportfolio) {
      return 'units.form.tooltip.notAvailableWithSubportfolio';
    }

    return 'units.form.tooltip.certificationNotAvailable';
  }

  get geoLocations(): UntypedFormArray {
    return this.form?.get('geoLocations') as UntypedFormArray;
  }

  get hasSubportfolio(): boolean {
    return !!this.data?.subportfolio;
  }

  get isAggregated(): boolean {
    return this.form?.get('aggregated')?.value;
  }

  get minValidTo(): Date {
    const validFrom = this.form?.get('validFrom')?.value;

    if (!validFrom) {
      return this.minDateTo;
    }

    return moment(validFrom).isBefore(moment(this.minDateTo)) ? this.minDateTo : validFrom;
  }

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    private authService: AuthService,
    public config: DynamicDialogConfig,
    private unitsService: UnitsService,
    private service: UnitsDialogService
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    this.unitsService.getDerTypesWithType().subscribe((response: Array<DerTypeDTO & { type: DerType }>) => {
      const unitTypes: { [key: string]: Array<DerTypeDTO & { type: DerType }> } = {};

      this.dictionaries.types.forEach(dict => {
        const derType = dict.value as DerType;

        unitTypes[this.getTypeKey('', dict)] = response.filter(({ type }) => type === derType);
      });

      this.unitTypes = unitTypes;
    });

    if (this.mode === 'edit') {
      this.unitsService.getUnit(this.config.data.id).subscribe((response: UnitDTO) => {
        this.data = response;

        this.createForm(response);
      });
    } else {
      this.createForm(this.config.data);
    }
  }

  addLocationPoint(): void {
    this.geoLocations.push(this.service.createGeoLocationForm());
  }

  changeTypeCheckbox(index: number, { checked }: { checked: boolean }): void {
    const type = this.dictionaries.types[index];
    const control = this.form?.get(this.getTypeKey('derType', type));

    if (checked) {
      control?.enable();
    } else {
      control?.disable();
      control?.setValue(null);
    }

    control?.updateValueAndValidity();
  }

  changeMainLocation(index: number): void {
    (this.geoLocations.controls as UntypedFormGroup[]).forEach((control: UntypedFormGroup, controlIndex: number) => {
      if (controlIndex === index) {
        return;
      }

      control.get('mainLocation')?.setValue(false);
    });
  }

  getTypeKey(prefix: string, dictionary: Dictionary): string {
    return camelCase(`${prefix}_${dictionary.value}`);
  }

  removeLocationPoint(index: number): void {
    this.geoLocations.removeAt(index);
  }

  save(): void {
    let method: Observable<void>;
    let error = false;

    if (!this.form) {
      return;
    }

    this.form.get('validFrom')?.updateValueAndValidity();

    if (!this.isAggregated && this.geoLocations.value?.length > 1) {
      error = true;
      this.toastr.warning('units.warning.maxOneGeoLocations');
    } else if (this.isAggregated && this.geoLocations.value?.length >= 1) {
      const someMainLocation = this.geoLocations.getRawValue().some(({ mainLocation }) => !!mainLocation);

      if (!someMainLocation) {
        error = true;
        this.toastr.warning('units.warning.noMainLocation');
      }
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');

      const data = this.form.getRawValue();

      if (!data.hasEnergyStorage && !data.hasGeneration && !data.hasReception) {
        this.toastr.warning('units.warning.chooseOneType');
      }

      return;
    } else if (error) {
      return;
    }

    const {
      aggregated,
      geoLocations,
      validFrom,
      validTo,
      couplingPointIdType,
      powerStationTypes,
      pointOfConnectionWithLvTypes,
      ...formData
    } = this.form.getRawValue();

    const data = {
      ...formData,
      aggregated,
      couplingPointIdTypes: couplingPointIdType ? [couplingPointIdType] : [],
      powerStationTypes: powerStationTypes ? [powerStationTypes] : [],
      pointOfConnectionWithLvTypes: pointOfConnectionWithLvTypes ? [pointOfConnectionWithLvTypes] : [],
      geoLocations: geoLocations
        .filter(({ latitude, longitude }: any) => Helpers.isFilled(latitude) || Helpers.isFilled(longitude))
        .map(({ mainLocation, ...rest }: UnitGeoLocationDTO) => ({
          mainLocation: aggregated ? mainLocation : false,
          ...rest,
        })),
      validTo: moment(validTo).set({ m: 0, s: 0, ms: 0 }),
      validFrom: moment(validFrom).set({ m: 0, s: 0, ms: 0 }),
    };

    if (this.mode === 'add') {
      method = this.unitsService.save(data);
    } else {
      method = this.unitsService.update(this.config.data.id, data);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error(`units.actions.${this.mode}.error`);
        })
      )
      .subscribe(() => {
        this.toastr.success(`units.actions.${this.mode}.success`);
        this.close(true);
      });
  }

  private createForm(data: UnitDTO): void {
    this.authService
      .hasAnyRoles([Role.ROLE_ADMIN, Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR])
      .then((hasRole: boolean) => {
        this.form = this.service.createForm(data, hasRole);
        this.initSubscribe();
      });
  }

  private initSubscribe(): void {
    this.subscribeAggregated();
    this.subscribeCertified();
    this.subscribeCertifiedAvailable();
    this.subscribeDateTo();
  }

  private subscribeAggregated(): void {
    if (!this.form) {
      return;
    }

    this.form
      .get('aggregated')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((value: boolean) => {
        if (!value && this.geoLocations.controls.length > 1) {
          this.geoLocations.controls.forEach((_, index: number) => {
            if (index === 0) {
              return;
            }

            Helpers.removeExcessFormGroup(this.geoLocations, 1);
          });
        } else if (!!value) {
          this.geoLocations.at(0)?.get('mainLocation')?.setValue(true);
        }

        if (!value) {
          this.geoLocations.at(0)?.get('mainLocation')?.setValue(false);
        }
      });
  }

  private subscribeCertified(): void {
    this.form
      ?.get('certified')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((certified: boolean) => {
        if (!this.form) {
          return;
        }

        if (certified) {
          Helpers.setValidatorToControls(this.form, ['couplingPointIdType', 'powerStationTypes', 'code'], [Validators.required]);
        } else {
          Helpers.setValidatorToControls(this.form, ['couplingPointIdType', 'powerStationTypes', 'code'], []);
        }
      });
  }

  private subscribeCertifiedAvailable(): void {
    const couplingPointIdChanges = this.form?.get('couplingPointIdType')?.valueChanges.pipe(distinctUntilChanged()) || of();
    const powerStationChanges = this.form?.get('powerStationTypes')?.valueChanges.pipe(distinctUntilChanged()) || of();
    const codeChanges = this.form?.get('code')?.valueChanges.pipe(distinctUntilChanged()) || of();
    const pmin = this.form?.get('pmin')?.valueChanges.pipe(distinctUntilChanged()) || of();
    const qmin = this.form?.get('qmin')?.valueChanges.pipe(distinctUntilChanged()) || of();
    const qmax = this.form?.get('qmax')?.valueChanges.pipe(distinctUntilChanged()) || of();

    merge(couplingPointIdChanges, powerStationChanges, codeChanges, pmin, qmin, qmax)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (!this.certifiedAvailable) {
          this.form?.get('certified')?.disable();
        } else {
          this.form?.get('certified')?.enable();
        }
      });
  }

  private subscribeDateTo(): void {
    this.form
      ?.get('validTo')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((date: Date) => {
        const disabledActive = date ? moment(date).isSameOrBefore(moment()) : false;
        const control = this.form?.get('active');

        this.authService.hasRole(Role.ROLE_ADMIN).then((hasRole: boolean) => {
          if (!hasRole && this.data?.certified) {
            return;
          }

          this.authService.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR).then((hasRole: boolean) => {
            if (hasRole) {
              return;
            }

            if (disabledActive) {
              control?.disable();
              control?.setValue(false);
            } else {
              control?.enable();
            }
          });
        });
      });
  }
}
