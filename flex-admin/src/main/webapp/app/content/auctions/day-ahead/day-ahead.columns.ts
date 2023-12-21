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
    field: 'deliveryDate',
  },
  {
    field: 'status',
  },
  {
    field: 'offers',
    export: false,
  },
  {
    field: 'name',
  },
  {
    field: 'product',
    key: 'product.name',
  },
  {
    field: 'type',
  },
  {
    field: 'capacityGateOpeningTime',
  },
  {
    field: 'capacityGateClosureTime',
  },
  {
    field: 'energyGateOpeningTime',
  },
  {
    field: 'energyGateClosureTime',
  },
  {
    field: 'minDesiredCapacity',
  },
  {
    field: 'minDesiredEnergy',
  },
  {
    field: 'maxDesiredCapacity',
  },
  {
    field: 'maxDesiredEnergy',
  },
  {
    field: 'capacityAvailability',
  },
  {
    field: 'energyAvailability',
  },
  {
    field: 'firstAuctionDate',
  },
  {
    field: 'lastAuctionDate',
  },
  {
    field: 'createdDate',
  },
  {
    field: 'lastModifiedDate',
  },
  {
    field: 'createdBy',
  },
  {
    field: 'lastModifiedBy',
  },
  {
    field: 'delete',
    export: false,
  },
];
