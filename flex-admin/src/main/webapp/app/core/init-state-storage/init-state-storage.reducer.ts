import { camelCase } from 'lodash-es';

import { ActionReducer, INIT, UPDATE } from '@ngrx/store';

import { State } from '../core.state';
import { APP_PREFIX } from '../state';

/**
 * Action reducer for initialization state from localStorage
 */
export function initStateFromLocalStorage(reducer: ActionReducer<State>): ActionReducer<State> {
  return (state, action) => {
    const newState = reducer(state, action);

    if ([INIT.toString(), UPDATE.toString()].includes(action.type)) {
      const loadedState = loadInitialState();

      return { ...newState, ...loadedState };
    }

    return newState;
  };
}

/**
 * Load state from localStorage
 */
export function loadInitialState(): any {
  return Object.keys(localStorage).reduce((state: any, storageKey) => {
    if (!storageKey.includes(APP_PREFIX)) {
      return state;
    }

    const stateKeys = getStateKeys(storageKey);
    let currentStateRef = state;

    stateKeys.forEach((key, index) => {
      if (index === stateKeys.length - 1) {
        const value = localStorage.getItem(storageKey);

        if (value) {
          currentStateRef[key] = JSON.parse(value);
        }

        return state;
      }

      currentStateRef[key] = currentStateRef[key] || {};
      currentStateRef = currentStateRef[key];
    });

    return state;
  }, {});
}

/**
 * Get list of keys for the state
 *
 * @return list of keys for the state
 *
 */
export function getStateKeys(storageKey: string): string[] {
  return storageKey
    .replace(APP_PREFIX, '')
    .toLowerCase()
    .split('.')
    .map(key => camelCase(key));
}
