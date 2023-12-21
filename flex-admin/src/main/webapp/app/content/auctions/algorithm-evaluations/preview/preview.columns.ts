import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'id',
  },
  {
    field: 'auctionId',
  },
  {
    field: 'auctionName',
  },
  {
    field: 'product',
  },
  {
    field: 'companyName',
  },
  {
    field: 'status',
  },
  {
    field: 'onlyPrice',
    key: 'price',
  },
  {
    field: 'onlyVolume',
    key: 'volume',
  },
  {
    field: 'volumeDivisibility',
  },
  {
    field: 'deliveryPeriod',
  },
  {
    field: 'deliveryPeriodDivisibility',
  },
  {
    field: 'acceptedVolume',
  },
  {
    field: 'acceptedDeliveryPeriod',
  },
];
