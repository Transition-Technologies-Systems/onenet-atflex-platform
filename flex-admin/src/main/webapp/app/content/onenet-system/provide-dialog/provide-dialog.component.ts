import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { AppToastrService, ToastrMessage } from '@app/core';
import { DialogExtends } from '@app/shared';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { FileUpload } from 'primeng/fileupload';
import { ProvideDialogDTO } from './provide-dialog';
import { ProvideDialogService } from './provide-dialog.service';
import { Dictionary } from '@app/shared/models';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-provide-dialog',
  templateUrl: './provide-dialog.component.html',
  styleUrls: ['./provide-dialog.component.scss'],
})
export class ProvideDialogComponent extends DialogExtends implements OnInit {
  @ViewChild(FileUpload) fileUploader: FileUpload | null = null;

  form: UntypedFormGroup | undefined;
  data: Partial<ProvideDialogDTO> = {};
  offeredServicesDict: Dictionary[] = [];

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private fb: UntypedFormBuilder,
    private service: ProvideDialogService
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    this.service.getServicesDictionary().subscribe((dict: Dictionary[]) => {
      this.offeredServicesDict = dict;
    });
    let preparedFormData = {
      title: '',
      description: '',
      dataOfferingId: this.config.data.onenetId,
    };
    this.form = this.createForm(preparedFormData);
  }

  save(): void {
    if (this.form?.invalid) {
      this.form?.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }
    if (!this.data.filename && !this.data.file) {
      this.toastr.warning('provideDialog.send.noFile');
      return;
    }

    const formData = this.form?.getRawValue();
    const selectedService = this.offeredServicesDict.find(item => item.value === formData.dataOfferingId);
    const selectedServiceSplitted = selectedService ? selectedService.label?.split('(') : [];
    const selectedServiceName = selectedServiceSplitted && selectedServiceSplitted.length ? selectedServiceSplitted[0].trim() : '';

    let dataToSend = { ...this.data, ...formData, code: this.config.data.serviceCode };

    this.service
      .sendData(dataToSend)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }
          this.toastr.error('provideDialog.send.error');
        })
      )
      .subscribe(() => {
        this.toastr.success(new ToastrMessage({ msg: 'provideDialog.send.success', params: { name: selectedServiceName } }));
        this.close(true);
      });
  }

  createForm(data: Partial<ProvideDialogDTO> = {}): UntypedFormGroup {
    return this.fb.group({
      title: [data.title, [Validators.required, Validators.maxLength(255)]],
      description: [data.description, []],
      dataOfferingId: [{ value: data.dataOfferingId ? data.dataOfferingId : null, disabled: !!data.dataOfferingId }, [Validators.required]],
    });
  }

  uploadModel({ currentFiles }: any): void {
    if (currentFiles.length) {
      this.data = { ...this.data, file: currentFiles[0], filename: currentFiles[0].name };
    } else {
      this.toastr.error(`error.provideDialog.wrongExtension`);
    }
    this.fileUploader?.clear();
  }

  clearUploader(): void {
    this.fileUploader?.clear();
    this.data.file = null;
    this.data.filename = null;
  }
}
