import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'actions',
    export: false,
  },
  {
    field: 'bidDate',
    key: 'createdDate',
  },
  {
    field: 'flexPotential',
  },
  {
    field: 'schedulingUnit',
  },
  {
    field: 'potentialFromFlex',
    key: 'flexPotential.id'
  },
  {
    field: 'potentialFromSU',
    key: 'schedulingUnit.id'
  },
  {
    field: 'status',
  },
  {
    field: 'price',
  },
  {
    field: 'priceKwh',
    key: 'price',
  },
  {
    field: 'volume',
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
