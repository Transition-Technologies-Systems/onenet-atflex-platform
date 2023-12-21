import { Component, OnInit } from '@angular/core';
import { AppToastrService, ToastrMessage } from '@app/core';
import { DialogExtends } from '@app/shared';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { KdmTimestampModelDTO } from '../dictionaries';
import { KdmModelDialogService } from './kdm-models-dialog.service';
import { ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { catchError, tap } from 'rxjs';

@Component({
  selector: 'app-kdm-models-dialog',
  templateUrl: './kdm-models-dialog.component.html',
  styleUrls: ['./kdm-models-dialog.component.scss'],
  providers: [ConfirmationService],
})
export class KdmModelsDialogComponent extends DialogExtends implements OnInit {
  areaName!: string;
  kdmModelId!: number;
  data!: KdmTimestampModelDTO[];

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private confirmationService: ConfirmationService,
    private translate: TranslateService,
    private kdmModelDialogService: KdmModelDialogService
  ) {
    super(ref, config);
    this.areaName = this.config.data.model.areaName;
    this.kdmModelId = this.config.data.model.id;
  }

  ngOnInit(): void {
    this.getTimestamps();
  }

  getTimestamps(): void {
    this.data = this.kdmModelDialogService.getInitialData(this.kdmModelId);
    this.kdmModelDialogService.getModel(this.kdmModelId).subscribe((response: KdmTimestampModelDTO[]) => {
      if (response.length) {
        this.data.forEach((timestamp, index) => {
          const respMatchingRow = response.find(respTimestamp => respTimestamp.timestamp === timestamp.timestamp);
          if (!!respMatchingRow) {
            this.data[index] = respMatchingRow;
          }
        });
      }
    });
  }

  uploadModel({ currentFiles }: any, timestamp: string, fileUploader: any): void {
    if (this.kdmModelId) {
      if (currentFiles.length) {
        const oldRecordId = this.data.find(item => item.timestamp === timestamp)?.id ?? (null as any);
        this.kdmModelDialogService
          .verifyModel({ file: currentFiles[0], timestamp, kdmModelId: this.kdmModelId, kdmFileId: oldRecordId })
          .pipe(
            tap(() => {
              this.toastr.success(`dictionariesPage.kdmModelsDialog.upload.success`);
              const recordId = this.data.findIndex(item => item.timestamp === timestamp);
              this.data[recordId] = { ...this.data[recordId], fileDTO: currentFiles[0], fileName: currentFiles[0].name };
            }),
            catchError((err: any) => {
              if (err.status === 400 && err.error?.errorKey) {
                const message = new ToastrMessage({
                  msg: err.error?.errorKey,
                  params: {
                    value: err.error?.params,
                  },
                });
                this.toastr.error(message);
              }
              throw err;
            })
          )
          .subscribe();
      } else {
        this.toastr.error(`error.kdmModel.cannotAddKdmModelTimestampFileBecauseWrongExtension`);
      }
    }
    fileUploader.clear();
  }

  deleteModel(event: Event, row: KdmTimestampModelDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant(`dictionariesPage.kdmModelsDialog.delete.question`),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        const recordId = this.data.findIndex(item => item.timestamp === row.timestamp);
        this.data[recordId] = { ...this.data[recordId], fileName: null, fileDTO: null, id: null };
      },
    });
  }

  save(): void {
    this.kdmModelDialogService.uploadModel(this.data).pipe(
      tap(() => {
        this.getTimestamps();
        this.close();
      }),
      catchError((err: any) => {
        this.toastr.error(`dictionariesPage.kdmModelsDialog.upload.error`);
        throw err;
      })
    ).subscribe();
  }

  hasStatus(status: string, row: KdmTimestampModelDTO): boolean {
    switch (status) {
      case 'NEW':
        return !!row.fileDTO || (Object.keys(row).includes('id') && row.fileName === null);
      case 'INCLUDED':
        return !!row.id || row.id === 0;
      default:
        return false;
    }
  }
}
