import { AuctionOfferStatus, AuctionDayAheadType, AuctionType } from '../enums';

import { UnitMinDTO } from '@app/content/scheduling-units/scheduling-units';

export interface AuctionOfferDTO {
  id: number;
  status: AuctionOfferStatus;
  type: AuctionDayAheadType;
  auctionType: AuctionType;
  price: number;
  volume: number;
  volumeDivisibility: boolean;
  acceptedVolume: number;
  deliveryPeriodFrom: string;
  deliveryPeriodTo: string;
  deliveryPeriodDivisibility: boolean;
  acceptedDeliveryPeriodFrom: string;
  acceptedDeliveryPeriodTo: string;
  fsp: any;
  auctionId: number;
  isEdited: boolean;
  verifiedVolumesPercent?: number;

  der?: UnitMinDTO;

  auctionCmvc?: any;
  flexPotential?: any;

  auctionDayAhead?: any;
  schedulingUnit?: any;

  commonPriceOfBid: boolean;
  commonPrice: number;

  ders?: AuctionOfferDerDTO[];
}

export interface AuctionOfferBandCommonPriceDTO {
  id: number;
  bandNumber: string;
  commonPrice: number;
  hourNumber: string;
}

export interface AuctionOfferDerDTO {
  der?: UnitMinDTO;
  volume?: number;
  price?: number;
  acceptedVolume?: number;
  bandData?: AuctionOfferDerBandDataDTO[];
}

export interface AuctionOfferDerBandDataDTO {
  hourNumber: string;
  bandNumber: number;
  volume: number | null;
  acceptedVolume: number | null;
  price: number | null;
  acceptedPrice: number | null;
  isEdited: boolean | null;
  sourcePower?: number;
  pmin?: number;
  selfSchedule?: number;
}

export interface AuctionOfferDerBandDataForm {
  bandNumber: number;
  data: AuctionOfferDerBandDataDTO[];
}
