import { TemplateRef } from '@angular/core';
import { Dictionary } from '@app/shared/models';

import { FilterType } from './filter-type';

export interface FilterContext {
  name: string;
  type: FilterType;
  visible?: boolean;
  className: string | undefined;
  iconClass: string | undefined;
  controlName: string | undefined;
  showHeader: boolean;
  dictionaries: Dictionary[];
  template: TemplateRef<any> | null;
  translateDictionaries: boolean;
}
