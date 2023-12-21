import * as moment from 'moment';

import { AppToastrService, AuthService } from '@app/core';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable, of, catchError } from 'rxjs';
import { SchedulingUnitDTO, UnitMinDTO } from '../scheduling-units';
import { takeUntil, tap } from 'rxjs/operators';

import { ConfirmationService } from 'primeng/api';
import { DialogExtends } from '@app/shared';
import { Dictionary } from '@app/shared/models';
import { UntypedFormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { LocalizationTypeDTO } from '@app/content/dictionaries/dictionaries';
import { ModalService } from '@app/shared/commons';
import { Role } from '@app/shared/enums';
import { SchedulingUnitsConfirmDialogComponent } from './confirm';
import { SchedulingUnitsDialogService } from './dialog.service';
import { SchedulingUnitsService } from '../scheduling-units.service';

interface Dictionaries {
  schedulungUnitTypes$: Observable<Dictionary[]>;
  localizationTypes$: Observable<LocalizationTypeDTO[]>;
  companies$: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-scheduling-unit-dialog',
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.scss'],
  providers: [ConfirmationService, SchedulingUnitsDialogService],
})
export class SchedulingUnitsDialogComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;
  data: Partial<SchedulingUnitDTO> = {};
  ders: UnitMinDTO[] = [];

  dictionaries: Dictionaries = {
    schedulungUnitTypes$: this.schedulingUnitsService.getSchedulungUnitTypes(),
    localizationTypes$: of([]),
    companies$: this.service.getCompanies(),
  };

  acceptedDersAvailable = false;
  selectedFiles: File[] = [];
  removeFiles: number[] = [];

  get certifiedAvailable(): boolean {
    const readyForTests = this.form?.get('readyForTests')?.value;

    return !!readyForTests;
  }

  get isCertificationLocked(): boolean {
    const date = moment(this.data.certificationChangeLockedUntil);
    if (date.isValid()) {
      return date.isAfter(moment());
    }
    return false;
  }

  get certificationChangeLockedUntil(): string {
    return moment(this.data.certificationChangeLockedUntil).format('YYYY-MM-DD');
  }

  constructor(
    public ref: DynamicDialogRef,
    public cdr: ChangeDetectorRef,
    public toastr: AppToastrService,
    private authService: AuthService,
    public config: DynamicDialogConfig,
    private modalService: ModalService,
    private service: SchedulingUnitsDialogService,
    private schedulingUnitsService: SchedulingUnitsService
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    if (this.mode === 'edit') {
      this.schedulingUnitsService.getSchedulingDers(this.config.data.id).subscribe((response: Map<string, UnitMinDTO[]>) => {
        this.ders = Object.values(response).flatMap((value: UnitMinDTO[]) =>
          value.map((unit: UnitMinDTO) => ({
            ...unit,
            name: `${unit.name} (${unit.fspCompanyName})`,
          }))
        );

        this.setDers();
      });

      this.schedulingUnitsService.getSchedulingUnits(this.config.data.id).subscribe((response: SchedulingUnitDTO) => {
        const unitIds = response.units?.map(({ id }) => id) ?? [];

        this.dictionaries.localizationTypes$ = this.schedulingUnitsService.getLocalizationsDict(unitIds).pipe(
          tap((response: LocalizationTypeDTO[]) => {
            this.form?.get('couplingPoints')?.setValue(response);

            if (!response.length) {
              this.form?.get('primaryCouplingPoint')?.disable();
            }
          })
        );

        this.data = response;
        this.createForm(response);
        this.setDers();
      });
    } else {
      this.createForm(this.config.data);
    }
  }

  getCouplingPoints(): string {
    const couplingPoints = this.data.couplingPoints ?? [];

    return couplingPoints.map(({ name }) => name).join(', ');
  }

  getSelectedLocalizations(): Dictionary[] {
    const couplingPoints: LocalizationTypeDTO[] = this.form?.get('couplingPoints')?.value ?? [];
    const couplingPointIds = couplingPoints.map(({ id }) => id);
    const data = this.data.couplingPoints ?? [];

    return data.filter(({ id }) => couplingPointIds.includes(id));
  }

  onChangeFileSelected(files: File[]): void {
    this.selectedFiles = files;
  }

  onDownloadFile(id: number): void {
    this.schedulingUnitsService.downloadFile(id);
  }

  onRemoveFileChange(ids: number[]): void {
    this.removeFiles = ids;
  }

  save(): void {
    if (!this.form) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    const { bspId, acceptedDers, ...formData } = this.form.getRawValue();
    const derIds = this.ders.map(({ id }) => id);

    const dersToRemove = derIds.filter((id: number) => !acceptedDers.includes(id));

    if (dersToRemove.length) {
      const dialog = this.modalService.open(SchedulingUnitsConfirmDialogComponent, { data: { ders: this.ders, dersToRemove } });

      dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
        if (!!result) {
          this.saveDialog(formData, bspId, dersToRemove);
        } else {
          this.close();
        }
      });
    } else {
      this.saveDialog(formData, bspId, dersToRemove);
    }
  }

  private createForm(data: SchedulingUnitDTO): void {
    this.form = this.service.createForm(data);
    this.initSubscribe();

    this.checkAcceptedDersAvailable();
  }

  private checkAcceptedDersAvailable(): void {
    this.authService
      .hasAnyRoles([Role.ROLE_ADMIN, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR, Role.ROLE_BALANCING_SERVICE_PROVIDER])
      .then((hasRole: boolean) => {
        this.acceptedDersAvailable = hasRole;

        if (!hasRole) {
          this.form?.get('acceptedDers')?.disable();
        }

        this.cdr.markForCheck();
      });
  }

  private initSubscribe(): void {
    this.subscribeDateTo();
    this.subscribeReadyForTests();
    this.subscribeCouplingPointIds();
  }

  private saveDialog(formData: any, bspId: number, dersToRemove: number[]): void {
    let method: Observable<void>;

    const data = {
      ...formData,
      bsp: { id: bspId },
      removeFiles: this.removeFiles,
    };

    if (this.mode === 'add') {
      method = this.schedulingUnitsService.save(data, this.selectedFiles);
    } else {
      method = this.schedulingUnitsService.update(this.config.data.id, data, this.selectedFiles, dersToRemove);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error(`schedulingUnits.actions.${this.mode}.error`);
        })
      )
      .subscribe(() => {
        this.toastr.success(`schedulingUnits.actions.${this.mode}.success`);
        this.close(true);
      });
  }

  private setDers(): void {
    const control = this.form?.get('acceptedDers');

    if (!!control) {
      const ids = this.ders.map(({ id }) => id);

      control.setValue(ids);
    }
  }

  private subscribeCouplingPointIds(): void {
    this.form
      ?.get('couplingPoints')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((couplingPoints: number[]) => {
        if (couplingPoints.length) {
          this.form?.get('primaryCouplingPoint')?.enable();
        } else {
          this.form?.get('primaryCouplingPoint')?.disable();
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

        if (disabledActive) {
          control?.disable();
          control?.setValue(false);
        } else {
          control?.enable();
        }
      });
  }

  private subscribeReadyForTests(): void {
    this.form
      ?.get('readyForTests')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((readyForTest: boolean) => {
        if (readyForTest && !this.isCertificationLocked) {
          this.form?.get('certified')?.enable();
        } else {
          this.form?.get('certified')?.disable();
        }
      });
  }
}
