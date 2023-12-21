import * as AuthActions from '@app/core/auth/actions';
import { UserDTO } from '@app/shared/models';
import { createReducer, on } from '@ngrx/store';

export const authFeatureKey = 'auth';

export interface State {
  isAuthenticated: boolean;
  pendingAuthentication: boolean;
  jwt: string | null;
  user: UserDTO | undefined;
}

export const initialState: State = {
  isAuthenticated: false,
  pendingAuthentication: false,
  user: undefined,
  jwt: null,
};

export const reducer = createReducer(
  initialState,
  on(AuthActions.login, (state, { jwt }) => ({
    ...state,
    pendingAuthentication: true,
    jwt,
  })),
  on(AuthActions.loginSuccess, (state, { jwt, user }) => ({
    ...state,
    jwt,
    user,
    pendingAuthentication: false,
    isAuthenticated: true,
  })),
  on(AuthActions.loginFailure, AuthActions.logout, AuthActions.invalidJWT, state => ({
    ...state,
    isAuthenticated: false,
    pendingAuthentication: false,
    jwt: null,
    user: undefined,
  })),
  on(AuthActions.updateUser, (state, { user }) => ({
    ...state,
    user,
  })),
  on(AuthActions.updateUserField, (state, { fieldName, newValue }) => {
    let user = { ...state.user } as UserDTO;

    if (user && Object.keys(user).includes(fieldName)) {
      user = {
        ...state.user,
        [fieldName]: newValue,
      } as UserDTO;
    }

    return {
      ...state,
      user,
    };
  }),
  on(AuthActions.changePassword, state => ({
    ...state,
    user: state.user
      ? {
          ...state.user,
          passwordChangeOnFirstLogin: false,
        }
      : undefined,
  }))
);
