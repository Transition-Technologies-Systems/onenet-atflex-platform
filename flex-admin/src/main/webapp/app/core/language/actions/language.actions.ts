import { createAction, props } from '@ngrx/store';

import { Language } from '@app/shared/enums';

export const languageChange = createAction(
  '[Language] Change language',
  props<{ key: Language; userLangKey?: Language; fromAccount?: boolean }>()
);
