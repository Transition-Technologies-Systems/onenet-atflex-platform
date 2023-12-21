import * as fromAuth from './auth/reducers/auth.reducer';
import * as fromConfiguration from './configuration/reducers/configuration.reducer';
import * as fromLanguage from './language/reducers/language.reducer';
import * as fromRouter from '@ngrx/router-store';

import { Action, ActionReducer, ActionReducerMap, MetaReducer, createFeatureSelector } from '@ngrx/store';

import { InjectionToken } from '@angular/core';
import { Language } from '@app/shared/enums';
import { environment } from '@env/environment';
import { initStateFromLocalStorage } from './init-state-storage';

export interface State {
  [fromLanguage.languageFeatureKey]: Language;
  router: fromRouter.RouterReducerState<any>;
  [fromAuth.authFeatureKey]: fromAuth.State;
}

export const ROOT_REDUCERS = new InjectionToken<ActionReducerMap<State, Action>>('Root reducers token', {
  factory: () => ({
    [fromLanguage.languageFeatureKey]: fromLanguage.reducer,
    [fromAuth.authFeatureKey]: fromAuth.reducer,
    [fromConfiguration.featureKey]: fromConfiguration.reducer,
    router: fromRouter.routerReducer,
  }),
});

export function logger(reducer: ActionReducer<State>): ActionReducer<State> {
  return (state, action) => {
    const result = reducer(state, action);
    console.groupCollapsed(action.type);
    console.log('prev state', state);
    console.log('action', action);
    console.log('next state', result);
    console.groupEnd();

    return result;
  };
}

export const metaReducers: MetaReducer<State>[] = [initStateFromLocalStorage];

if (!environment.production) {
  metaReducers.unshift(logger);
}

export const getRouterState = createFeatureSelector<fromRouter.RouterReducerState<any>>('router');

export const getLanguageState = createFeatureSelector<Language>('language');

export const getAuthState = createFeatureSelector<fromAuth.State>('auth');

export const getConfigurationState = createFeatureSelector<fromConfiguration.State>('configuration');
