import { Renderer2, TemplateRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { NgbOffcanvas, NgbOffcanvasRef } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
  type MockedObject
} from 'vitest';
import { DocumentRef } from '../../core/services/browser/document-ref.service';
import { WindowRef } from '../../core/services/browser/window-ref.service';
import {
  HEADER_OFFCANVAS,
  HEADER_OFFCANVAS_BACKDROP,
  HEADER_OFFCANVAS_GOOGLE_SEARCH_BAR_ID,
  LARGE_BREAKPOINT
} from '../constants/common.constant';
import { GoogleSearchBarUtils } from '../utils/google-search-bar.utils';
import { HeaderOffcanvasService } from './header-offcanvas.service';

const buildOffcanvasRef = () => {
  const shownSubject = new Subject<void>();
  let resolveResult!: () => void;
  const resultPromise = new Promise<void>(resolve => {
    resolveResult = resolve;
  });

  const ref = {
    shown: shownSubject.asObservable(),
    result: resultPromise,
    close: vi.fn()
  } as unknown as NgbOffcanvasRef;

  return { ref, shownSubject, resolveResult };
};

describe('HeaderOffcanvasService', () => {
  let service: HeaderOffcanvasService;
  let offcanvasSpy: MockedObject<NgbOffcanvas>;
  let windowRefSpy: { nativeWindow: Window | undefined };
  let documentRefSpy: { nativeDocument: Document | undefined };
  let renderer: MockedObject<Renderer2>;
  let content: TemplateRef<any>;

  beforeEach(() => {
    offcanvasSpy = { open: vi.fn() } as unknown as MockedObject<NgbOffcanvas>;
    windowRefSpy = { nativeWindow: window };
    documentRefSpy = { nativeDocument: document };
    renderer = {
      createElement: vi.fn(),
      appendChild: vi.fn()
    } as unknown as MockedObject<Renderer2>;
    content = {} as TemplateRef<any>;

    TestBed.configureTestingModule({
      providers: [
        HeaderOffcanvasService,
        { provide: NgbOffcanvas, useValue: offcanvasSpy },
        { provide: WindowRef, useValue: windowRefSpy },
        { provide: DocumentRef, useValue: documentRefSpy }
      ]
    });

    service = TestBed.inject(HeaderOffcanvasService);
  });

  describe('open()', () => {
    it('should open the offcanvas with correct options', () => {
      const { ref } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      service.open(content, renderer);

      expect(offcanvasSpy.open).toHaveBeenCalledWith(content, {
        ariaLabelledBy: HEADER_OFFCANVAS,
        backdrop: true,
        panelClass: HEADER_OFFCANVAS,
        position: 'end',
        backdropClass: HEADER_OFFCANVAS_BACKDROP
      });
    });

    it('should do nothing when nativeDocument is undefined', () => {
      documentRefSpy.nativeDocument = undefined;

      service.open(content, renderer);

      expect(offcanvasSpy.open).not.toHaveBeenCalled();
    });

    it('should call GoogleSearchBarUtils methods when shown fires', () => {
      const { ref, shownSubject } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      vi.spyOn(
        GoogleSearchBarUtils,
        'renderGoogleSearchBar'
      ).mockImplementation(() => {});
      vi.spyOn(
        GoogleSearchBarUtils,
        'addCustomClassToSearchBar'
      ).mockImplementation(() => {});

      service.open(content, renderer);
      shownSubject.next();

      expect(GoogleSearchBarUtils.renderGoogleSearchBar).toHaveBeenCalledWith(
        renderer,
        windowRefSpy,
        documentRefSpy,
        HEADER_OFFCANVAS_GOOGLE_SEARCH_BAR_ID
      );
      expect(
        GoogleSearchBarUtils.addCustomClassToSearchBar
      ).toHaveBeenCalledWith(renderer, document);
    });

    it('should set offcanvasRef to null after result resolves', async () => {
      const { ref, resolveResult } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      service.open(content, renderer);
      expect(service.isOpen()).toBe(true);

      resolveResult();
      await ref.result;

      expect(service.isOpen()).toBe(false);
    });
  });

  describe('isOpen()', () => {
    it('should return false when no offcanvas is open', () => {
      expect(service.isOpen()).toBe(false);
    });

    it('should return true when an offcanvas is open', () => {
      const { ref } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      service.open(content, renderer);

      expect(service.isOpen()).toBe(true);
    });
  });

  describe('close()', () => {
    it('should call close on the offcanvas ref and set it to null', () => {
      const { ref } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      service.open(content, renderer);
      service.close();

      expect(ref.close).toHaveBeenCalled();
      expect(service.isOpen()).toBe(false);
    });

    it('should not throw when called without an open offcanvas', () => {
      expect(() => service.close()).not.toThrow();
    });
  });

  describe('toggle()', () => {
    it('should do nothing when window width is >= LARGE_BREAKPOINT', () => {
      vi.spyOn(window, 'innerWidth', 'get').mockReturnValue(
        LARGE_BREAKPOINT
      );

      service.toggle(content, renderer);

      expect(offcanvasSpy.open).not.toHaveBeenCalled();
    });

    it('should open when offcanvas is not open and width < LARGE_BREAKPOINT', () => {
      vi.spyOn(window, 'innerWidth', 'get').mockReturnValue(
        LARGE_BREAKPOINT - 1
      );
      const { ref } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      service.toggle(content, renderer);

      expect(offcanvasSpy.open).toHaveBeenCalled();
    });

    it('should close when offcanvas is open and width < LARGE_BREAKPOINT', () => {
      vi.spyOn(window, 'innerWidth', 'get').mockReturnValue(
        LARGE_BREAKPOINT - 1
      );
      const { ref } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      service.open(content, renderer);
      service.toggle(content, renderer);

      expect(ref.close).toHaveBeenCalled();
    });
  });

  describe('handleResize()', () => {
    it('should close the offcanvas when width >= LARGE_BREAKPOINT and offcanvas is open', () => {
      vi.spyOn(window, 'innerWidth', 'get').mockReturnValue(
        LARGE_BREAKPOINT
      );
      const { ref } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      service.open(content, renderer);
      service.handleResize();

      expect(ref.close).toHaveBeenCalled();
      expect(service.isOpen()).toBe(false);
    });

    it('should do nothing when width < LARGE_BREAKPOINT', () => {
      vi.spyOn(window, 'innerWidth', 'get').mockReturnValue(
        LARGE_BREAKPOINT - 1
      );
      const { ref } = buildOffcanvasRef();
      offcanvasSpy.open.mockReturnValue(ref);

      service.open(content, renderer);
      service.handleResize();

      expect(ref.close).not.toHaveBeenCalled();
    });

    it('should do nothing when offcanvas is not open', () => {
      vi.spyOn(window, 'innerWidth', 'get').mockReturnValue(
        LARGE_BREAKPOINT
      );

      expect(() => service.handleResize()).not.toThrow();
    });
  });
});
