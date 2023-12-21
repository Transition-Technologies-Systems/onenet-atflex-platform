import { FilterContext } from './filter-context';

export interface FilterTabContext {
  tabName: string;
  visible?: boolean;
  filters: FilterContext[];
}
