import { TestBed } from '@angular/core/testing';
import { DocumentRef } from './document-ref.service';
import { PLATFORM_ID } from '@angular/core';

describe('SessionStorageRef', () => {
  describe('in browser platform', () => {
    let service: DocumentRef;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          DocumentRef,
          { provide: PLATFORM_ID, useValue: 'browser' } // ✅ Simulate browser
        ]
      });
      service = TestBed.inject(DocumentRef);
    });

    it('should return the global document in browser', () => {
      expect(service.nativeDocument).toBe(document);
    });
  });

  describe('in server platform', () => {
    let service: DocumentRef;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          DocumentRef,
          { provide: PLATFORM_ID, useValue: 'server' } // ❌ Simulate server
        ]
      });
      service = TestBed.inject(DocumentRef);
    });

    it('should return undefined in server', () => {
      expect(service.nativeDocument).toBeUndefined();
    });
  });
});
