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
    field: 'name',
  },
  {
    field: 'directionOfDeviation',
  },
  {
    field: 'sourcePower',
  },
  {
    field: 'connectionPower',
  },
  {
    field: 'pmin',
  },
  {
    field: 'qmin',
  },
  {
    field: 'qmax',
  },
  {
    field: 'derType',
  },
  {
    field: 'sder',
  },
  {
    field: 'aggregated',
  },
  {
    field: 'fspId',
    key: 'fsp.representative.companyName',
  },
  {
    field: 'createdDate',
  },
  {
    field: 'lastModifiedDate',
  },
  {
    field: 'validFrom',
  },
  {
    field: 'validTo',
  },
  {
    field: 'subportfolio',
  },
  {
    field: 'schedulingUnit',
    key: 'schedulingUnit.name',
  },
  {
    field: 'ppe',
  },
  {
    field: 'powerStationTypes',
  },
  {
    field: 'couplingPointIdTypes',
  },
  {
    field: 'code',
  },
  {
    field: 'mridTso',
  },
  {
    field: 'mridDso',
  },
  {
    field: 'active',
  },
  {
    field: 'certified',
  },
  {
    field: 'delete',
    export: false,
  },
];
