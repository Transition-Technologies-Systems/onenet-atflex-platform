import { AuctionOfferStatus, AuctionDayAheadType, AuctionType } from '../enums';

export interface MyOffersDTO {
  id: number;
  status: AuctionOfferStatus;
  type: AuctionDayAheadType;
  auctionId: number;
  auctionType: AuctionType;
  auctonId: number;
  price: number;
  volume: number;
  volumeTooltipVisible: boolean;
  volumeDivisibility: boolean;
  acceptedVolume: number;
  acceptedVolumeTooltipVisible: boolean;
  deliveryPeriodFrom: string;
  deliveryPeriodTo: string;
  deliveryPeriodDivisibility: boolean;
  acceptedDeliveryPeriodFrom: string;
  acceptedDeliveryPeriodTo: string;
  verifiedVolumesPercent: number;
  schedulingUnitOrPotential: string;
  derMinDTOs: DerMinDTO[];
  flexibilityPotentialVolume: string;

  auctionName: string;
  companyName: string;
  fspId: number;
  offerCategory: AuctionType;
  productId: number;
  productName: string;
  submitMoment: string;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface DerMinDTO {
  id: number;
  name: string;
  pmin: number;
  sourcePower: number;
}
