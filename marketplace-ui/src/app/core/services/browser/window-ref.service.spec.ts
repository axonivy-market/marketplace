import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { WindowRef } from './window-ref.service';

describe('SessionStorageRef', () => {
  describe('in browser platform', () => {
    let service: WindowRef;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          WindowRef,
          { provide: PLATFORM_ID, useValue: 'browser' }
        ]
      });
      service = TestBed.inject(WindowRef);
    });

    it('should return the global document in browser', () => {
      expect(service.nativeWindow).toBe(window);
    });
  });

  describe('in server platform', () => {
    let service: WindowRef;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          WindowRef,
          { provide: PLATFORM_ID, useValue: 'server' }
        ]
      });
      service = TestBed.inject(WindowRef);
    });

    it('should return undefined in server', () => {
      expect(service.nativeWindow).toBeUndefined();
    });
  });
});
