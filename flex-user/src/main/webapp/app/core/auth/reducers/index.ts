import { createSelector } from '@ngrx/store';

import { getAuthState } from '../../core.state';

export * from './auth.reducer';

export const isAuthenticated = createSelector(getAuthState, state => ({
  authenticated: state.isAuthenticated,
  passwordChangeOnFirstLogin: !!state.user?.passwordChangeOnFirstLogin,
}));

export const pendingAuthentication = createSelector(getAuthState, state => state.pendingAuthentication);

export const getUserData = createSelector(getAuthState, state => state.user);
export const getUserLang = createSelector(getAuthState, state => state?.user?.langKey || null);
