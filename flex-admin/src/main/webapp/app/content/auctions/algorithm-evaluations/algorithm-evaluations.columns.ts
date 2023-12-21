import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'id',
  },
  {
    field: 'typeOfAlgorithm',
  },
  {
    field: 'deliveryDate',
  },
  {
    field: 'kdmModelName',
  },
  {
    field: 'bids',
  },
  {
    field: 'createdDate',
  },
  {
    field: 'endDate',
  },
  {
    field: 'inputFiles',
    export: false,
  },
  {
    field: 'outputFiles',
    export: false,
  },
  {
    field: 'processLogs',
    export: false,
  },
  {
    field: 'status',
  },
  {
    field: 'parseResults',
    export: false,
  },
];
