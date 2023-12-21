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
    field: 'productShortName',
    header: 'productId',
    key: 'product.shortName',
  },
  {
    field: 'fspRepresentativeCompanyName',
    header: 'fspId',
    key: 'fsp.representative.companyName',
  },
  {
    field: 'unitName',
    header: 'unitId',
    key: 'unit.name',
  },
  {
    field: 'volume',
  },
  {
    field: 'volumeUnit',
  },
  {
    field: 'divisibility',
  },
  {
    field: 'fullActivationTime',
  },
  {
    field: 'minDeliveryDuration',
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
    field: 'validFrom',
  },
  {
    field: 'validTo',
  },
  {
    field: 'active',
  },
  {
    field: 'productPrequalification',
  },
  {
    field: 'staticGridPrequalification',
  },
  {
    field: 'delete',
    export: false,
  },
];
