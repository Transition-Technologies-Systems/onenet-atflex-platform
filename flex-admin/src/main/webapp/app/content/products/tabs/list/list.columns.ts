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
    field: 'fullName',
  },
  {
    field: 'shortName',
  },
  {
    field: 'locational',
  },
  {
    field: 'minBidSize',
  },
  {
    field: 'maxBidSize',
  },
  {
    field: 'bidSizeUnit',
  },
  {
    field: 'direction',
  },
  {
    field: 'maxFullActivationTime',
  },
  {
    field: 'minRequiredDeliveryDuration',
  },
  {
    field: 'lastModifiedDate',
  },
  {
    field: 'createdDate',
  },
  {
    field: 'active',
  },
  {
    field: 'balancing',
  },
  {
    field: 'cmvc',
  },
  {
    field: 'validFrom',
  },
  {
    field: 'validTo',
  },
  {
    field: 'delete',
    export: false,
  },
];
