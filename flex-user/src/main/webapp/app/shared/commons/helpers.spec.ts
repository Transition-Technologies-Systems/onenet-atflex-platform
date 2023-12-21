import { Observable } from 'rxjs';

import { NgZone, SimpleChange, SimpleChanges } from '@angular/core';
import { fakeAsync, tick } from '@angular/core/testing';

import { Helpers } from './helpers';

describe('Helpers', () => {
  it('should normalize string', () => {
    const value = 'ąćĘż';
    expect(Helpers.normalize(value)).toBe('acez');
  });

  describe('anyChanges', () => {
    const properties = ['columns', 'key'];

    it('should check changes and return true', () => {
      const changes = {
        columns: {} as SimpleChange,
      } as SimpleChanges;

      expect(Helpers.anyChanges(properties, changes)).toBeTruthy();
    });

    it('should check changes and return false', () => {
      const changes = {
        data: {} as SimpleChange,
      } as SimpleChanges;

      expect(Helpers.anyChanges(properties, changes)).not.toBeTruthy();
    });
  });

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
