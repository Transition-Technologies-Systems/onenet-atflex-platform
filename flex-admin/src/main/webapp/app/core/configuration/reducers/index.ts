import { createSelector } from '@ngrx/store';
import { getConfigurationState } from '@app/core/core.state';

export * from './configuration.reducer';

export const showObligedToTakePartInBalancingEnergyInformation = createSelector(
  getConfigurationState,
  state => state.obligated && !state.submittedOffer
);
