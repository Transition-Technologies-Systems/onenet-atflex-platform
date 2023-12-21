import { HttpService } from '@app/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { DictionaryLangDto } from '../dictionaries';
import { DictionaryType } from '@app/shared/enums';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';

@Injectable({
  providedIn: 'root',
})
export class DictDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(data: Partial<DictionaryLangDto> = {}, type: DictionaryType): UntypedFormGroup {
    const form = this.fb.group({
      id: [{ value: data.id, disabled: true }],
    });

    if (type !== DictionaryType.LOCALIZATION_TYPE && type !== DictionaryType.KDM_MODEL) {
      form.addControl('descriptionEn', this.fb.control(data.descriptionEn, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]));
      form.addControl('descriptionPl', this.fb.control(data.descriptionPl, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]));
    }

    switch (type) {
      case DictionaryType.DER_TYPE:
        form.addControl('sderPoint', this.fb.control(data.sderPoint));
        form.addControl('type', this.fb.control(data.type, [Validators.required]));
        break;
      case DictionaryType.SCHEDULING_UNIT_TYPE:
        form.addControl('products', this.fb.control(data.products ?? []));
        break;
      case DictionaryType.LOCALIZATION_TYPE:
        form.addControl('name', this.fb.control(data.name, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]));
        form.addControl('type', this.fb.control(data.type, [Validators.required]));
        break;
      case DictionaryType.KDM_MODEL:
        form.addControl(
          'areaName',
          this.fb.control(data.areaName, [Validators.required, RequiredNoWhitespaceValidator, Validators.maxLength(50)])
        );
        form.addControl('lvModel', this.fb.control(data.lvModel, []));
    }

    return form;
  }
}
