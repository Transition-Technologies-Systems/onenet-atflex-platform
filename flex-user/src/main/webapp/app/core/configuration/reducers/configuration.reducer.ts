import * as AuthActions from '@app/core/auth/actions';
import * as ConfigurationActions from '@app/core/configuration/actions';

import { createReducer, on } from '@ngrx/store';

import { OfferReminder } from '@app/shared/models';

export const featureKey = 'configuration';

export interface State {
  obligated: boolean;
  submittedOffer: boolean;
  data: OfferReminder | null;
}

export const initialState: State = {
  obligated: false,
  submittedOffer: false,
  data: null,
};

export const reducer = createReducer(
  initialState,
  on(ConfigurationActions.obligedToTakePartInBalancingEnergy, (state, { obligated }) => ({
    ...state,
    obligated,
  })),
  on(ConfigurationActions.offerHasBeenSubmittedInBalancingEnergy, (state, { submittedOffer }) => ({
    ...state,
    submittedOffer,
  })),
  on(ConfigurationActions.updateReminder, (state, { data }) => ({
    ...state,
    data,
  })),
  on(AuthActions.loginSuccess, state => ({
    ...state,
    data: {
      auctionGateClosureTime: null,
      auctionGateOpeningTime: null,
      auctionId: null,
      auctionName: null,
      hasReminder: false,
    },
  }))
);
