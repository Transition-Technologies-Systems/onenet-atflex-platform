import { catchError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { AppToastrService } from '@app/core';
import { DialogExtends } from '@app/shared';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { OnsUserDTO } from '../ons-users';
import { OnsUsersService } from '../ons-users.service';

@Component({
  selector: 'app-ons-users-dialog',
  templateUrl: './ons-users-dialog.component.html',
})
export class OnsUsersDialogComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;

  data: Partial<OnsUserDTO> = {};

  constructor(
    public ref: DynamicDialogRef,
    public cdr: ChangeDetectorRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private service: OnsUsersService,
    private fb: UntypedFormBuilder
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    this.form = this.createForm(this.config.data, this.mode);
  }

  save(): void {
    if (this.form?.invalid) {
      this.form?.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }
    let formData = this.form?.getRawValue();
    this.service
      .addUser(formData)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error('onsUsers.actions.add.error');
        })
      )
      .subscribe(() => {
        this.toastr.success('onsUsers.actions.add.success');
        this.close(true);
      });
  }

  createForm(data: Partial<OnsUserDTO> = {}, mode: 'add' | 'edit'): UntypedFormGroup {
    return this.fb.group({
      username: [data.username, [RequiredNoWhitespaceValidator, Validators.required, Validators.maxLength(255)]],
      password: [data.password, [RequiredNoWhitespaceValidator, Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
    });
  }
}
