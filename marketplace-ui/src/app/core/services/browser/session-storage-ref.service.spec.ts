import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { SessionStorageRef } from './session-storage-ref.service';

describe('SessionStorageRef', () => {
  describe('in browser platform', () => {
    let service: SessionStorageRef;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          SessionStorageRef,
          { provide: PLATFORM_ID, useValue: 'browser' } // ✅ Simulate browser
        ]
      });
      service = TestBed.inject(SessionStorageRef);
    });

    it('should return the global document in browser', () => {
      expect(service.session).toBe(sessionStorage);
    });
  });

  describe('in server platform', () => {
    let service: SessionStorageRef;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          SessionStorageRef,
          { provide: PLATFORM_ID, useValue: 'server' } // ❌ Simulate server
        ]
      });
      service = TestBed.inject(SessionStorageRef);
    });

    it('should return undefined in server', () => {
      expect(service.session).toBeNull();
    });
  });
});
