import { Language } from '@app/shared/enum';
import { UserDTO } from '@app/shared/model';

import { AuthState } from '../auth/auth.models';
import { State } from '../core.state';
import { LocalStorageService } from '../local-storage/local-storage.service';
import { getStateKeys, loadInitialState } from './init-state-storage.reducer';

describe('InitStateFromLocalStorage', () => {
  let state: State;
  let storageService: LocalStorageService;

  beforeEach(() => {
    state = createState('en', {
      jwt: 'test-jwt',
      isAuthenticated: true,
      user: { id: 1 } as UserDTO,
    });
    storageService = new LocalStorageService();

    localStorage.clear();

    storageService.setItem('language', state.language);
    storageService.setItem('auth', state.auth);
  });

  it('should get saved state in local storage', () => {
    const loadedState = loadInitialState();
    expect(loadedState).toEqual(state);
  });

  it('should return state keys for key', () => {
    expect(getStateKeys('APP-language')).toEqual(['language']);
  });

  it('should return state keys for key with .', () => {
    expect(getStateKeys('APP-language.key')).toEqual(['language', 'key']);
  });

  it('should return state keys for key with -', () => {
    expect(getStateKeys('APP-language-key')).toEqual(['languageKey']);
  });
});

function createState(languageState: Language, authState: AuthState) {
  return {
    auth: authState,
    language: languageState,
  } as State;
}
