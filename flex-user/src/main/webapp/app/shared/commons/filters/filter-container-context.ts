import { FilterContext } from './filter-context';
import { FilterTabContext } from './filter-tab-context';

export interface FilterContainerContext {
  group: string;
  filters: FilterContext[] | null;
  tabs: FilterTabContext[] | null;
}
