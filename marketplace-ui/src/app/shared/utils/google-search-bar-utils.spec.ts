import { Renderer2 } from '@angular/core';
import { WindowRef } from '../../core/services/browser/window-ref.service';
import { DocumentRef } from '../../core/services/browser/document-ref.service';
import { GoogleSearchBarUtils } from './google-search-bar.utils';
import {
  GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_SOURCE,
  GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_TYPE,
  GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID,
  GOOGLE_SEARCH,
  GOOGLE_SEARCH_BAR_BACKGROUND_CLASS_NAME,
  GOOGLE_SEARCH_BAR_CLASS_NAME
} from '../constants/common.constant';

describe('GoogleSearchBarUtils', () => {
  let mockRenderer: jasmine.SpyObj<Renderer2>;
  let mockWindowRef: jasmine.SpyObj<WindowRef>;
  let mockDocumentRef: jasmine.SpyObj<DocumentRef>;
  let mockDocument: jasmine.SpyObj<Document>;
  let mockWindow: jasmine.SpyObj<Window>;

  beforeEach(() => {
    mockRenderer = jasmine.createSpyObj('Renderer2', [
      'createElement',
      'appendChild',
      'addClass'
    ]);

    mockWindowRef = jasmine.createSpyObj('WindowRef', ['toString'], {
      nativeWindow: undefined
    });

    mockDocumentRef = jasmine.createSpyObj('DocumentRef', ['toString'], {
      nativeDocument: undefined
    });

    mockDocument = jasmine.createSpyObj(
      'Document',
      ['getElementById', 'querySelectorAll'],
      {
        body: jasmine.createSpyObj('HTMLElement', ['appendChild'])
      }
    );

    mockWindow = jasmine.createSpyObj('Window', ['toString'], {
      google: {
        search: {
          cse: {
            element: {
              render: jasmine.createSpy('render')
            }
          }
        }
      }
    });

    spyOn(globalThis, 'setTimeout').and.callFake(((fn: Function) => {
      fn();
      return 1;
    }) as any);
  });

  describe('renderGoogleSearchBar', () => {
    it('should return early if document is not available', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => undefined
      });

      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      expect(mockRenderer.createElement).not.toHaveBeenCalled();
    });

    it('should create and append script element when googleCSEScript does not exist', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => mockWindow
      });

      const mockScript = jasmine.createSpyObj(
        'HTMLScriptElement',
        ['setAttribute'],
        {
          onload: undefined
        }
      );

      mockDocument.getElementById.and.returnValue(null);
      mockRenderer.createElement.and.returnValue(mockScript);

      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      expect(mockDocument.getElementById).toHaveBeenCalledWith(
        GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID
      );
      expect(mockRenderer.createElement).toHaveBeenCalledWith('script');
      expect(mockRenderer.appendChild).toHaveBeenCalledWith(
        mockDocument.body,
        mockScript
      );

      expect(mockScript.id).toBe(GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID);
      expect(mockScript.type).toBe(GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_TYPE);
      expect(mockScript.async).toBe(true);
      expect(mockScript.src).toBe(GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_SOURCE);
    });

    it('should not create script element when googleCSEScript already exists', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => mockWindow
      });

      const existingScript = jasmine.createSpyObj('HTMLScriptElement', [
        'getAttribute'
      ]);
      mockDocument.getElementById.and.returnValue(existingScript);

      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      expect(mockRenderer.createElement).not.toHaveBeenCalled();
      expect(mockRenderer.appendChild).not.toHaveBeenCalled();
    });

    it('should call google.search.cse.element.render when google search is available', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => mockWindow
      });

      mockDocument.getElementById.and.returnValue(null);
      mockRenderer.createElement.and.returnValue(
        jasmine.createSpyObj('HTMLScriptElement', ['setAttribute'])
      );

      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      expect(mockWindow.google.search.cse.element.render).toHaveBeenCalledWith(
        GOOGLE_SEARCH
      );
    });

    it('should not call google.search.cse.element.render when google search is not available', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });

      const windowWithoutGoogle = jasmine.createSpyObj('Window', ['toString']);
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => windowWithoutGoogle
      });

      mockDocument.getElementById.and.returnValue(null);
      mockRenderer.createElement.and.returnValue(
        jasmine.createSpyObj('HTMLScriptElement', ['setAttribute'])
      );

      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      expect(() => {
        GoogleSearchBarUtils.renderGoogleSearchBar(
          mockRenderer,
          mockWindowRef,
          mockDocumentRef
        );
      }).not.toThrow();
    });

    it('should trigger addCustomClassToSearchBar when script loads', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => mockWindow
      });

      const mockScript = { onload: undefined } as any;
      mockDocument.getElementById.and.returnValue(null);
      mockRenderer.createElement.and.returnValue(mockScript);

      spyOn(GoogleSearchBarUtils, 'addCustomClassToSearchBar');

      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      mockScript.onload();

      expect(
        GoogleSearchBarUtils.addCustomClassToSearchBar
      ).toHaveBeenCalledWith(mockRenderer, mockDocument);
    });

    it('should NOT trigger addCustomClassToSearchBar when script loads if Document is undefined', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => undefined
      });
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => mockWindow
      });

      let onloadHandler: (() => void) | undefined;

      const mockScript = {
        set onload(fn: () => void) {
          onloadHandler = fn;
        }
      } as any;

      mockDocument.getElementById.and.returnValue(null);
      mockRenderer.createElement.and.returnValue(mockScript);

      spyOn(GoogleSearchBarUtils, 'addCustomClassToSearchBar');

      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      if (onloadHandler) {
        onloadHandler();
      }

      expect(
        GoogleSearchBarUtils.addCustomClassToSearchBar
      ).not.toHaveBeenCalled();
    });
  });

  describe('addCustomClassToSearchBar', () => {
    it('should add custom class to search bar elements after timeout', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });

      const mockSearchBox1 = jasmine.createSpyObj('HTMLElement', ['classList']);
      const mockSearchBox2 = jasmine.createSpyObj('HTMLElement', ['classList']);
      const mockSearchBoxList = [mockSearchBox1, mockSearchBox2];

      mockDocument.querySelectorAll.and.returnValue(mockSearchBoxList as any);

      GoogleSearchBarUtils.addCustomClassToSearchBar(
        mockRenderer,
        mockDocument
      );

      expect(window.setTimeout).toHaveBeenCalledWith(
        jasmine.any(Function),
        1000
      );
      expect(mockDocument.querySelectorAll).toHaveBeenCalledWith(
        '.gsc-control-cse'
      );
      expect(mockRenderer.addClass).toHaveBeenCalledWith(
        mockSearchBox1,
        GOOGLE_SEARCH_BAR_BACKGROUND_CLASS_NAME
      );
      expect(mockRenderer.addClass).toHaveBeenCalledWith(
        mockSearchBox2,
        GOOGLE_SEARCH_BAR_BACKGROUND_CLASS_NAME
      );
    });

    it('should handle empty search box list gracefully', () => {
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });

      mockDocument.querySelectorAll.and.returnValue([] as any);

      GoogleSearchBarUtils.addCustomClassToSearchBar(
        mockRenderer,
        mockDocument
      );

      expect(mockDocument.querySelectorAll).toHaveBeenCalledWith(
        GOOGLE_SEARCH_BAR_CLASS_NAME
      );
      expect(mockRenderer.addClass).not.toHaveBeenCalled();
    });
  });
});
