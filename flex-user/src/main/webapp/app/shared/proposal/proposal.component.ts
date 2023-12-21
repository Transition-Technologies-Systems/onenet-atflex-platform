import { catchError } from 'rxjs/operators';
import { AppToastrService, AuthService } from '@app/core';
import { Component, OnInit } from '@angular/core';
import { Dictionary, FspDTO } from '@app/shared/models';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { DialogExtends } from '@app/shared';
import { UntypedFormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ProposalService } from './proposal.service';
import { Role } from '../enums';
import { SchedulingUnitDTO } from '@app/content/scheduling-units/scheduling-units';
import { takeUntil } from 'rxjs';

interface Dictionaries {
  units: Dictionary[];
  subportfolios: Dictionary[];
}

@Component({
  selector: 'app-bsp-proposal',
  templateUrl: './proposal.component.html',
})
export class ProposalComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;
  isFspa = false;

  dictionaries: Dictionaries = {
    units: [],
    subportfolios: [],
  };

  get bspId(): number | undefined {
    const bsp: FspDTO | undefined = this.config.data?.bsp;

    return bsp?.id ?? this.schedulingUnit?.bsp?.id;
  }

  get schedulingUnit(): SchedulingUnitDTO | undefined {
    return this.config.data?.schedulingUnit;
  }

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    private service: ProposalService,
    private authService: AuthService,
    public config: DynamicDialogConfig
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    this.authService.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED).then((isFspa: boolean) => {
      this.isFspa = isFspa;
      this.form = this.service.createForm();

      this.form.get('unitId')?.setValue(null);
      this.form.get('subportfolio')?.setValue(null);

      if (isFspa) {
        this.form.get('subportfolio')?.enable();

        this.getSubportfolios();
        this.subscribeSubportfolio();
      } else {
        this.form.get('subportfolio')?.disable();

        this.getUnits();
      }
    });
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

    const { subportfolio, ...formData } = this.form.getRawValue();
    const data = {
      ...formData,
      bspId: this.bspId,
    };

    this.service
      .saveProposal(data)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error(`shared.proposal.error`);
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success(`shared.proposal.success`);
        this.close(true);
      });
  }

  private getUnits(): void {
    if (!this.bspId) {
      return;
    }

    this.service.getUnits(this.bspId).subscribe(response => {
      this.dictionaries.units = response || [];
    });
  }

  private getSubportfolios(): void {
    this.service.getSubportfolios().subscribe(response => {
      this.dictionaries.subportfolios = response || [];
    });
  }

  private subscribeSubportfolio(): void {
    this.form
      ?.get('subportfolio')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((subportfolioId: number) => {
        this.service.getDers(subportfolioId, this.bspId).subscribe(response => {
          this.dictionaries.units = response || [];
        });
      });
  }
}
