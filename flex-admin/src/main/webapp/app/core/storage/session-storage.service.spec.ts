import { TestBed } from '@angular/core/testing';
import { SessionStorageService } from './session-storage.service';

describe('SessionStorageService', () => {
  let service: SessionStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SessionStorageService],
    });
    service = TestBed.inject(SessionStorageService);
  });

  afterEach(() => localStorage.clear());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should set item', () => {
    spyOn(service, 'setItem');
    service.setItem('test', 'value');
    expect(service.setItem).toHaveBeenCalled();
  });

  describe('string value', () => {
    beforeEach(() => {
      service.setItem('test', 'value');
    });

    it('should read item', () => {
      expect(service.getItem('test')).toBe('value');
    });

    it('should remove item', () => {
      service.removeItem('test');
      expect(service.getItem('test')).toBeNull();
    });
  });

  describe('object value', () => {
    const testValue = {
      test: 'value',
    };

    beforeEach(() => {
      service.setItem('test', testValue);
    });

    it('should read item', () => {
      expect(service.getItem('test')).toEqual(testValue);
    });

    it('should remove item', () => {
      service.removeItem('test');
      expect(service.getItem('test')).toBeNull();
    });
  });
});
