import { catchError } from 'rxjs/operators';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { AppToastrService } from '@app/core';
import { DialogExtends } from '@app/shared';
import { Dictionary } from '@app/shared/models';
import { HttpErrorResponse } from '@angular/common/http';
import { UnitsInviteDerService } from './invite-der.service';
import { UnitsService } from '../units.service';

interface Dictionaries {
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
    this.getScheduleUnits();
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

  private getScheduleUnits(): void {
    this.unitsService.getScheduleUnits(this.config.data?.id).subscribe((response: Dictionary[]) => {
      this.dictionaries.scheduleUnits = response;

      this.cdr.markForCheck();
    });
  }
}
