import { Injectable } from '@angular/core';

import { APP_PREFIX } from '../state';

/**
 * This is localStorage service
 */
@Injectable()
export class LocalStorageService {
  constructor() {}

  /**
   * Set object to localStorage
   *
   * @param key The item key
   * @param key The item value
   *
   * @returns Notnig
   * @example
   * LocalStorageService.setItem('app-name', 'test');
   */
  setItem(key: string, value: any): void {
    localStorage.setItem(`${APP_PREFIX}${key}`, JSON.stringify(value));
  }

  /**
   * Get object from localStorage
   *
   * @param key The item key
   * @returns Retur saved object
   *
   * @example
   * LocalStorageService.getItem('app-name');
   */
  getItem(key: string): any {
    const value = localStorage.getItem(`${APP_PREFIX}${key}`);

    if (!value) {
      return null;
    }

    return JSON.parse(value);
  }

  /**
   * Remove saved object from localStorage
   *
   * @param key The item key
   * @returns Notnig
   *
   * @example
   * LocalStorageService.removeItem('app-name');
   */
  removeItem(key: string): void {
    localStorage.removeItem(`${APP_PREFIX}${key}`);
  }

  /**
   * Clear localStorage
   */
  clear(): void {
    localStorage.clear();
  }
}
