import { fakeAsync, TestBed, tick } from '@angular/core/testing';

import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let loadingService: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [],
    });

    loadingService = new LoadingService();
  });

  it('should be created', () => {
    expect(loadingService).toBeTruthy();
  });

  it('should emit loading status true', () => {
    spyOn(loadingService.loading, 'next');

    loadingService.changeRouter();

    expect(loadingService.loading.next).toHaveBeenCalledWith(true);
  });

  it('should start loading', () => {
    spyOn(loadingService.loading, 'next');

    loadingService.startLoading();

    expect(loadingService.loading.next).toHaveBeenCalledWith(true);
  });

  describe('stopLoading', () => {
    it('should stop loading', () => {
      (loadingService as any).requestInProgress = 0;
      spyOn(loadingService.loading, 'next');

      loadingService.stopLoading();

      expect(loadingService.loading.next).toHaveBeenCalledWith(false);
    });

    it('should not stop loading', () => {
      (loadingService as any).requestInProgress = 1;
      spyOn(loadingService.loading, 'next');

      loadingService.stopLoading();

      expect(loadingService.loading.next).not.toHaveBeenCalledWith(false);
    });
  });

  describe('addRequest', () => {
    it('should add request and start loading', () => {
      (loadingService as any).requestInProgress = 0;

      spyOn(loadingService, 'startLoading');

      loadingService.addRequest();

      expect(loadingService.startLoading).toHaveBeenCalled();
    });

    it('should add request', () => {
      (loadingService as any).requestInProgress = 1;

      spyOn(loadingService, 'startLoading');

      loadingService.addRequest();

      expect(loadingService.startLoading).not.toHaveBeenCalled();
      expect((loadingService as any).requestInProgress).toBe(2);
    });
  });

  describe('removeRequest', () => {
    it('should remove request and stop loading', fakeAsync(() => {
      (loadingService as any).requestInProgress = 1;

      spyOn(loadingService.loading, 'next');

      loadingService.removeRequest();
      tick(200);

      expect(loadingService.loading.next).toHaveBeenCalledWith(false);
    }));

    it('should remove request', fakeAsync(() => {
      (loadingService as any).requestInProgress = 2;

      spyOn(loadingService.loading, 'next');

      loadingService.removeRequest();
      tick(200);

      expect(loadingService.loading.next).not.toHaveBeenCalled();
      expect((loadingService as any).requestInProgress).toBe(1);
    }));
  });

  describe('clearTimeout', () => {
    it('should clear timeout', () => {
      (loadingService as any).timeoutCancel = 1;

      spyOn(window, 'clearTimeout');

      loadingService.changeRouter();

      expect(window.clearTimeout).toHaveBeenCalled();
    });

    it('should not clear timeout', () => {
      (loadingService as any).timeoutCancel = 0;

      spyOn(window, 'clearTimeout');

      loadingService.changeRouter();

      expect(window.clearTimeout).not.toHaveBeenCalled();
    });
  });
});
