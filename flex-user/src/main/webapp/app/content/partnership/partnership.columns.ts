import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'actions',
    export: false,
  },
  {
    field: 'status',
    header: 'status',
  },
  {
    field: 'bspName',
    header: 'sender',
  },
  {
    field: 'fspName',
    header: 'receiver',
  },
  {
    field: 'schedulingUnitName',
    header: 'schedulingUnit',
  },
  {
    field: 'derName',
    header: 'der',
  },
  {
    field: 'sentDate',
    header: 'sentDate',
  },
  {
    field: 'lastModifiedDate',
    header: 'lastModifiedDate',
  }
];
