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
    field: 'pMin',
  },
  {
    field: 'qMin',
  },
  {
    field: 'qMax',
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
    key: 'subportfolio.name',
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
    field: 'pointOfConnectionWithLvTypes',
  },
  {
    field: 'code',
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
