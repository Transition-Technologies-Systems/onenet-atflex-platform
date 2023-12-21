import { Component, OnInit } from '@angular/core';
import { Dictionary, FspDTO } from '@app/shared/models';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable, take, takeUntil, catchError } from 'rxjs';

import { AppToastrService } from '@app/core';
import { DialogExtends } from '@app/shared';
import { HttpErrorResponse } from '@angular/common/http';
import { ProposalService } from './proposal.service';
import { Role } from '../enums';
import { SchedulingUnitDTO } from '@app/content/scheduling-units/scheduling-units';

interface Dictionaries {
  companies$: Observable<Dictionary[]>;
  subportfolios: Dictionary[];
  units: Dictionary[];
}

@Component({
  selector: 'app-proposal',
  templateUrl: './proposal.component.html',
  providers: [ProposalService],
})
export class ProposalComponent extends DialogExtends implements OnInit {
  schedulingUnit: SchedulingUnitDTO | undefined = this.config.data?.schedulingUnit;
  form = this.service.createForm();
  isFspa = false;

  dictionaries: Dictionaries = {
    companies$: this.service.getCompanies(this.bspId).pipe(take(1)),
    subportfolios: [],
    units: [],
  };

  get bspId(): number | undefined {
    const bsp: FspDTO | undefined = this.config.data?.bsp;

    return bsp?.id ?? this.schedulingUnit?.bsp?.id;
  }

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    private service: ProposalService,
    public config: DynamicDialogConfig
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    this.subscribeFspChange();
    this.subscribeSubportfolioChange();
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    const { fsp, subportfolio, ...formData } = this.form.getRawValue();
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

  private getSubportfolios(fsp?: FspDTO): void {
    const fspId = fsp?.id;

    this.dictionaries.subportfolios = [];

    if (fsp?.role !== Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED || !fspId || !this.bspId) {
      return;
    }

    this.service.getSubportfolios(fspId).subscribe(response => {
      this.dictionaries.subportfolios = response || [];
    });
  }

  private getUnits(): void {
    const fsp = this.form.get('fsp')?.value;
    const fspId = fsp?.id;

    this.dictionaries.units = [];

    if (!fspId || !this.bspId) {
      return;
    }

    const isFspa = fsp?.role === Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED;
    const subportfolioId = this.form.get('subportfolio')?.value;
    const fspaId = this.form.get('fsp')?.value.id;

    if (isFspa) {
      this.service.getDers(subportfolioId, this.bspId, fspaId).subscribe(response => {
        this.dictionaries.units = response || [];
      });

      return;
    }

    this.service.getUnits(this.bspId, fspId).subscribe(response => {
      this.dictionaries.units = response || [];
    });
  }

  private subscribeFspChange(): void {
    this.form
      .get('fsp')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((fsp: FspDTO) => {
        this.isFspa = fsp?.role === Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED;

        this.form.get('unitId')?.setValue(null);
        this.form.get('subportfolio')?.setValue(null);

        this.getUnits();
        this.getSubportfolios(fsp);

        if (this.isFspa) {
          this.form.get('subportfolio')?.enable();
        } else {
          this.form.get('subportfolio')?.disable();
        }
      });
  }

  private subscribeSubportfolioChange(): void {
    this.form
      .get('subportfolio')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(() => this.getUnits());
  }
}
