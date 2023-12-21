import * as languageActions from '@app/core/language/actions';
import { createReducer, on } from '@ngrx/store';

export const languageFeatureKey = 'language';

export const initialState: any = 'en';

export const reducer = createReducer(
  initialState,
  on(languageActions.languageChange, (state, { key }) => key || state)
);
