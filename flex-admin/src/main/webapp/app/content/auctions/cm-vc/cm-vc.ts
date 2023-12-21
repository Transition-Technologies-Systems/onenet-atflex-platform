import { AuctionCmvcType, AuctionStatus } from '../enums';

import { LocalizationTypeDTO } from '@app/content/dictionaries/dictionaries';
import { ProductDTO } from '@app/shared/models';

export interface AuctionCmvcDTO {
  id: number;
  name: string;
  productName: string;
  localization: LocalizationTypeDTO[];
  deliveryDate: string;
  deliveryDateFrom: string;
  deliveryDateTo: string;
  gateOpeningTime: string;
  gateClosureTime: string;
  minDesiredPower: number;
  maxDesiredPower: number;
  status: AuctionStatus;
  auctionCmvcType: AuctionCmvcType;
  product: ProductDTO;
  productId: number;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;

  offers: { companyName: string; potentialId: number; potentialName: string; id: number; price: string; volume: string }[];

  canAddBid: boolean;
}
