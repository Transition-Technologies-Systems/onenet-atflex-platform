import { Observable } from 'rxjs';

import { NgZone, SimpleChange, SimpleChanges } from '@angular/core';
import { fakeAsync, tick } from '@angular/core/testing';

import { Helpers } from './helpers';

describe('Helpers', () => {

  describe('runOnStableZone', () => {
    const fn = {
      callback: () => {
        console.log('isStable');
      },
    };

    it('should run function, ngZone is stable', () => {
      const zone = { isStable: true } as NgZone;

      spyOn(console, 'log');

      Helpers.runOnStableZone(zone, fn.callback);

      expect(console.log).toHaveBeenCalledTimes(1);
    });

    it('should run function when ngZone is stable', fakeAsync(() => {
      const onStable = new Observable(observer => {
        setTimeout(() => observer.next(true), 200);
        setTimeout(() => observer.next(true), 400);
      });
      const zone = { isStable: false, onStable } as NgZone;

      spyOn(console, 'log');

      Helpers.runOnStableZone(zone, fn.callback);

      tick(200);
      tick(400);

      expect(console.log).toHaveBeenCalledTimes(1);
    }));
  });
});
