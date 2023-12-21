import { createAction, props } from '@ngrx/store';

import { OfferReminder } from '@app/shared/models';

export const obligedToTakePartInBalancingEnergy = createAction(
  '[Configuration] Obligation to take part in balancing energy',
  props<{ obligated: boolean }>()
);

export const offerHasBeenSubmittedInBalancingEnergy = createAction(
  '[Configuration] Offer has been submited in balancing energy',
  props<{ submittedOffer: boolean }>()
);

export const updateReminder = createAction('[Configuration] Update offer reminder', props<{ data: OfferReminder }>());
