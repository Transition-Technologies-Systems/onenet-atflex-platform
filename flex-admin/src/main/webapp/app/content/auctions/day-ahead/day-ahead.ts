import { AuctionDayAheadType, AuctionStatus } from '../enums';

import { ProductDTO } from '@app/shared/models';

export interface AuctionDayAheadDTO {
  id: number;
  name: string;
  status: AuctionStatus;
  day: string;
  type: AuctionDayAheadType;
  productId: number;
  productName: string;
  product: ProductDTO;
  canAddBid: boolean;
  deliveryDate: string;

  energyGateOpeningTime: string;
  energyGateClosureTime: string;
  capacityGateOpeningTime: string;
  capacityGateClosureTime: string;
  capacityAvailabilityFrom: string;
  capacityAvailabilityTo: string;
  energyAvailabilityFrom: string;
  energyAvailabilityTo: string;
  auctionSeriesId: number;

  maxDesiredCapacity: number;
  maxDesiredEnergy: number;
  minDesiredCapacity: number;
  minDesiredEnergy: number;
  minDesiredPower?: number;
  maxDesiredPower?: number;

  offers: {
    companyName: string;
    potentialId: number;
    potentialName: string;
    id: number;
    price: string;
    volume: string;
  }[];

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface AuctionsSeriesDTO {
  id: number;
  name: string;
  product: ProductDTO;
  type: AuctionDayAheadType;

  energyGateOpeningTime: string;
  energyGateClosureTime: string;
  capacityGateOpeningTime: string;
  capacityGateClosureTime: string;
  capacityAvailabilityFrom: string;
  capacityAvailabilityTo: string;
  energyAvailabilityFrom: string;
  energyAvailabilityTo: string;
  firstAuctionDate: string;
  lastAuctionDate: string;

  maxDesiredCapacity: number;
  maxDesiredEnergy: number;
  minDesiredCapacity: number;
  minDesiredEnergy: number;

  deletable: boolean;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface Tab {
  label: string;
  type: TabType;
}

export type TabType = 'energy-auctions' | 'capacity-auctions' | 'series-auctions';
