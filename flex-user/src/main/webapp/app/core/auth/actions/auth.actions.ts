import { UserDTO } from '@app/shared/models';
import { createAction, props } from '@ngrx/store';

export const login = createAction('[Auth] Login', props<{ jwt: string; initialization?: boolean }>());

export const loginSuccess = createAction('[Auth] Login success', props<{ jwt: string; user: UserDTO; initialization: boolean }>());

export const loginFailure = createAction('[Auth] Login failure');

export const logout = createAction('[Auth] Logout');

export const invalidJWT = createAction('[Auth] Invalid JWT');

export const updateUser = createAction('[Auth] Update user', props<{ user: UserDTO }>());

export const updateUserField = createAction('[Auth] Update user field', props<{ fieldName: string; newValue: any }>());

export const changePassword = createAction('[Auth] Change user password');
