import { createSelector } from '@ngrx/store';
import { getConfigurationState } from '@app/core/core.state';

export * from './configuration.reducer';

export const offerUpdateReminder = createSelector(getConfigurationState, state => state.data);
