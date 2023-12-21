import { AlgorithmEvaluationConfigDTO, Dictionary } from '@app/shared/models';
import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { UntypedFormBuilder, Validators } from '@angular/forms';

import { AppToastrService } from '@app/core';
import { BidsEvaluationService } from '../bids-evaluation.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { AlgorithmType } from '@app/shared/enums';

interface Dictionaries {
  kdmTypes$: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-run-algorithm',
  templateUrl: './run-algorithm.component.html',
  providers: [],
})
export class BidsEvaluationRunAlgorithmDialogComponent {
  form = this.fb.group({
    kdmModelId: [null, Validators.required],
    kdmLvModel: [{ value: null, disabled: true }],
  });

  dictionaries: Dictionaries = {
    kdmTypes$: this.service.getKDMModels(),
  };

  get algorithmConfig(): Partial<AlgorithmEvaluationConfigDTO> {
    return this.config.data ?? {};
  }

  constructor(
    private fb: UntypedFormBuilder,
    public ref: DynamicDialogRef,
    private toastr: AppToastrService,
    private translate: TranslateService,
    public config: DynamicDialogConfig,
    private service: BidsEvaluationService
  ) {}

  onKDMModelChange(event: any): void {
    this.form.patchValue({ kdmLvModel: event.lvModel });
  }

  close(): void {
    this.ref.close();
  }

  run(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    const data = {
      ...this.algorithmConfig,
      ...this.form.getRawValue(),
    };

    this.service
      .runAlgorithm(data, this.algorithmConfig.algorithmType as AlgorithmType)
      .pipe(
        catchError(({ status, error }: HttpErrorResponse): any => {
          if (!(status === 400 && error?.errorKey)) {
            this.toastr.error('auctions.actions.bidsEvaluation.runAlgorithm.error');
            this.close();
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success('auctions.actions.bidsEvaluation.runAlgorithm.success');
        this.close();
      });
  }
}
