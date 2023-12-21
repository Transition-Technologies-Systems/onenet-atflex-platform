import { AuctionOfferDTO, AuctionOfferDerBandDataDTO, AuctionOfferDerDTO } from '../../offers/offer';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { FormType, Nullable } from '@app/shared/models';

import { UnitMinDTO } from '@app/content/scheduling-units/scheduling-units';

type AuctionOfferOmitKeys =
  | 'status'
  | 'type'
  | 'ders'
  | 'isEdited'
  | 'auctionType'
  | 'deliveryPeriodFrom'
  | 'deliveryPeriodTo'
  | 'acceptedDeliveryPeriodFrom'
  | 'acceptedDeliveryPeriodTo'
  | 'acceptedVolume'
  | 'volume'
  | 'price';

export type BidFormType = FormType<Nullable<Omit<AuctionOfferDTO, AuctionOfferOmitKeys>>> & {
  deliveryPeriod: FormControl<Date>;
  deliveryPeriodFrom: FormControl<Date | null>;
  deliveryPeriodTo: FormControl<Date | null>;
  acceptedDeliveryPeriod: FormControl<Date | null>;
  acceptedDeliveryPeriodFrom: FormControl<Date | null>;
  acceptedDeliveryPeriodTo: FormControl<Date | null>;

  bandNumber: FormControl<number | null>;
  hourNumber: FormControl<number | null>;

  acceptedVolume?: FormControl<number | null>;
  volume?: FormControl<number | null>;
  price?: FormControl<number | null>;

  ders?: FormArray<FormGroup<BidDerFormType>>;
};

export type BidDerFormType = FormType<Nullable<Omit<AuctionOfferDerDTO, 'bandData'>>> & {
  derDisabled: FormControl<UnitMinDTO | null>;
  bandData?: FormArray<FormGroup<BidDerBandFormType>>;
};

export type BidDerBandFormType = {
  bandNumber: FormControl<number>;
  data: FormArray<FormGroup<BidDerBandDataFormType>>;
};

export type BidDerBandDataFormType = FormType<AuctionOfferDerBandDataDTO>;
