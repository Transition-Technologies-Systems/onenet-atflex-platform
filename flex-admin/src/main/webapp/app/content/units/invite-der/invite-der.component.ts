import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { AppToastrService } from '@app/core';
import { DialogExtends } from '@app/shared';
import { Dictionary } from '@app/shared/models';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { UnitsInviteDerService } from './invite-der.service';
import { UnitsService } from '../units.service';
import { takeUntil } from 'rxjs/operators';

interface Dictionaries {
  companies$: Observable<Dictionary[]>;
  scheduleUnits: Dictionary[];
}

@Component({
  selector: 'app-units-invite-der-proposal',
  templateUrl: './invite-der.component.html',
  providers: [UnitsInviteDerService],
})
export class UnitsInviteDerComponent extends DialogExtends implements OnInit {
  form = this.service.createForm();

  dictionaries: Dictionaries = {
    companies$: this.service.getCompanies(),
    scheduleUnits: [],
  };

  constructor(
    public ref: DynamicDialogRef,
    private cdr: ChangeDetectorRef,
    public toastr: AppToastrService,
    private unitsService: UnitsService,
    public config: DynamicDialogConfig,
    private service: UnitsInviteDerService
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    this.subscribeBspChange();
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    const formData = this.form.getRawValue();

    const data = {
      ...formData,
      unitId: this.config.data?.id,
    };

    this.unitsService
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

  private getScheduleUnits(bspId: number, derId: number): void {
    this.dictionaries.scheduleUnits = [];

    if (!bspId || !derId) {
      return;
    }

    this.service.getScheduleUnits(bspId, derId).subscribe((response: Dictionary[]) => {
      this.dictionaries.scheduleUnits = response;

      this.cdr.markForCheck();
    });
  }

  private subscribeBspChange(): void {
    this.form
      .get('bspId')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((bspId: number) => {
        this.form.get('schedulingUnitId')?.setValue(null);
        this.getScheduleUnits(bspId, this.config.data?.id);
      });
  }
}
