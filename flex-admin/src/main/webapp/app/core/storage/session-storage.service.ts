import { Injectable } from '@angular/core';

import { APP_PREFIX } from '../state';

/**
 * This is sessionStorage service
 */
@Injectable()
export class SessionStorageService {
  constructor() {}

  /**
   * Set object to sessionStorage
   *
   * @param key The item key
   * @param key The item value
   *
   * @returns Notnig
   * @example
   * sessionStorageService.setItem('app-name', 'test');
   */
  setItem(key: string, value: any): void {
    sessionStorage.setItem(`${APP_PREFIX}${key}`, JSON.stringify(value));
  }

  /**
   * Get object from sessionStorage
   *
   * @param key The item key
   * @returns Retur saved object
   *
   * @example
   * sessionStorageService.getItem('app-name');
   */
  getItem(key: string): any {
    const value = sessionStorage.getItem(`${APP_PREFIX}${key}`);

    if (!value) {
      return null;
    }

    return JSON.parse(value);
  }

  /**
   * Remove saved object from sessionStorage
   *
   * @param key The item key
   * @returns Notnig
   *
   * @example
   * sessionStorageService.removeItem('app-name');
   */
  removeItem(key: string): void {
    sessionStorage.removeItem(`${APP_PREFIX}${key}`);
  }

  /**
   * Clear sessionStorage
   */
  clear(): void {
    sessionStorage.clear();
  }
}
