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
    field: 'localization',
  },
  {
    field: 'deliveryDate',
  },
  {
    field: 'gateOpeningTime',
  },
  {
    field: 'gateClosureTime',
  },
  {
    field: 'minDesiredPower',
  },
  {
    field: 'maxDesiredPower',
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
