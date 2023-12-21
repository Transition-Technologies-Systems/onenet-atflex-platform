import { createReducer } from '@ngrx/store';

export const featureKey = 'configuration';

export interface State {}

export const initialState: State = {};

export const reducer = createReducer(initialState);
