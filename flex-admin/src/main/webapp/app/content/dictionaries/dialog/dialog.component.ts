import { Component, OnInit } from '@angular/core';
import { DerType, DictionaryType, LocalizationType } from '@app/shared/enums';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { AppToastrService } from '@app/core';
import { DialogExtends } from '@app/shared';
import { DictDialogService } from './dialog.service';
import { DictionariesService } from '../dictionaries.service';
import { Dictionary } from '@app/shared/models';
import { DictionaryLangDto } from '../dictionaries';
import { UntypedFormGroup } from '@angular/forms';
import { Helpers } from '@app/shared/commons';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';

interface Dictionaries {
  products$: Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>>;
  localizationTypes: Dictionary[];
  derTypes: Dictionary[];
}

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
})
export class DictDialogComponent extends DialogExtends implements OnInit {
  nowDate = new Date();
  form: UntypedFormGroup | undefined;

  dictionaries: Dictionaries = {
    products$: this.dictService.getProducts(),
    derTypes: Helpers.enumToDictionary(DerType, 'DerType'),
    localizationTypes: Helpers.enumToDictionary(LocalizationType, 'LocalizationType'),
  };

  dictionaryType: DictionaryType = this.config.data.type;

  get isDerType(): boolean {
    return this.dictionaryType === DictionaryType.DER_TYPE;
  }

  get isSuType(): boolean {
    return this.dictionaryType === DictionaryType.SCHEDULING_UNIT_TYPE;
  }

  get isLocalizationType(): boolean {
    return this.dictionaryType === DictionaryType.LOCALIZATION_TYPE;
  }

  get isKdmModel(): boolean {
    return this.dictionaryType === DictionaryType.KDM_MODEL;
  }

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private dictService: DictionariesService,
    private service: DictDialogService
  ) {
    super(ref, config);

    this.mode = this.config.data?.model?.id ? 'edit' : 'add';
  }

  ngOnInit(): void {
    if (this.mode === 'edit') {
      this.dictService.getPositionDetails(this.config.data.model.id).subscribe((response: DictionaryLangDto) => {
        this.createForm(response);
      });
    } else {
      this.createForm(this.config.data.model);
    }
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

    const formData = this.form.getRawValue();

    if (this.mode === 'add') {
      method = this.dictService.save(formData);
    } else {
      method = this.dictService.update(this.config.data.model.id, formData);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error(`dictionariesPage.actions.${this.mode}.error`);
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success(`dictionariesPage.actions.${this.mode}.success`);
        this.close(true);
      });
  }

  private createForm(data: DictionaryLangDto): void {
    this.form = this.service.createForm(data, this.dictionaryType);
  }
}
