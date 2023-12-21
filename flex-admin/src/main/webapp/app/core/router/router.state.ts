import { Params } from '@angular/router';

/**
 * Router state
 */
export interface RouterStateUrl {
  /**
   * Current URL
   */
  url: string;
  /**
   * Current params
   */
  params: Params;
  /**
   * Current query params
   */
  queryParams: Params;
}
