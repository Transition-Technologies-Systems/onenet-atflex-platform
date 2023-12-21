import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'actions',
    export: false,
  },
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
    field: 'status',
  },
  {
    field: 'schedulingUnitOrPotential'
  },
  {
    field: 'auctionStatus',
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
