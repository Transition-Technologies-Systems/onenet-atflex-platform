import { Language } from '@app/shared/enums';
import { UserDTO } from '@app/shared/models';
import { AuthState } from '../auth/auth.models';
import { State } from '../core.state';
import { LocalStorageService } from '../storage/local-storage.service';
import { getStateKeys, loadInitialState } from './init-state-storage.reducer';

describe('InitStateFromLocalStorage', () => {
  let state: State;
  let storageService: LocalStorageService;
  const appName = 'FLEX-ADMIN';

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
    expect(getStateKeys(`${appName}-language`)).toEqual(['language']);
  });

  it('should return state keys for key with .', () => {
    expect(getStateKeys(`${appName}-language.key`)).toEqual(['language', 'key']);
  });

  it('should return state keys for key with -', () => {
    expect(getStateKeys(`${appName}-language-key`)).toEqual(['languageKey']);
  });
});

function createState(languageState: Language, authState: AuthState) {
  return {
    auth: authState,
    language: languageState,
  } as State;
}
