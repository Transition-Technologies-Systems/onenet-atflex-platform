import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { AppToastrService } from '@app/core';
import { DialogExtends } from '@app/shared';
import { ActivationsSettlementsDTO } from '@app/shared/models/activations-settlements';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable, catchError } from 'rxjs';
import { ActivationsSettlementsService } from '../activations-settlements.service';
import { ActivationsSettlementsDialogService } from './dialog.service';

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
})
export class ActivationsSettlementsDialogComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;
  data!: ActivationsSettlementsDTO;

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private service: ActivationsSettlementsService,
    private dialogService: ActivationsSettlementsDialogService
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    this.service.getActivationSettlementMinVersion(this.config.data.id).subscribe((response: ActivationsSettlementsDTO) => {
      this.data = response;
      this.form = this.dialogService.createForm(response);
    });
  }

  save(): void {
    let method: Observable<void>;

    if (!this.form) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    let formData = this.form.getRawValue();
    method = this.service.update(this.config.data.id, formData);

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error('activationsSettlements.dialog.actions.edit.error');
        })
      )
      .subscribe(() => {
        this.toastr.success(`activationsSettlements.dialog.actions.${this.mode}.success`);
        this.close(true);
      });
  }
}
