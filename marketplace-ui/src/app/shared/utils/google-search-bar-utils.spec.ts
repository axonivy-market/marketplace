import type { MockedObject } from 'vitest';
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
  let mockRenderer: MockedObject<Renderer2>;
  let mockWindowRef: MockedObject<WindowRef>;
  let mockDocumentRef: MockedObject<DocumentRef>;
  let mockDocument: MockedObject<Document>;
  let mockWindow: MockedObject<Window>;

  beforeEach(() => {
    mockRenderer = {
      createElement: vi.fn().mockName('Renderer2.createElement'),
      appendChild: vi.fn().mockName('Renderer2.appendChild'),
      addClass: vi.fn().mockName('Renderer2.addClass')
    };

    mockWindowRef = {
      toString: vi.fn().mockName('WindowRef.toString'),
      nativeWindow: undefined
    };

    mockDocumentRef = {
      toString: vi.fn().mockName('DocumentRef.toString'),
      nativeDocument: undefined
    };

    mockDocument = {
      getElementById: vi.fn().mockName('Document.getElementById'),
      querySelectorAll: vi.fn().mockName('Document.querySelectorAll'),
      body: {
        appendChild: vi.fn().mockName('HTMLElement.appendChild')
      }
    };

    mockWindow = {
      toString: vi.fn().mockName('Window.toString'),
      google: {
        search: {
          cse: {
            element: {
              render: vi.fn()
            }
          }
        }
      }
    };

    vi.spyOn(globalThis, 'setTimeout').mockImplementation(((fn: Function) => {
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

      const mockScript = {
        setAttribute: vi.fn().mockName('HTMLScriptElement.setAttribute'),
        onload: undefined
      };

      mockDocument.getElementById.mockReturnValue(null);
      mockRenderer.createElement.mockReturnValue(mockScript);

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

      const existingScript = {
        getAttribute: vi.fn().mockName('HTMLScriptElement.getAttribute')
      };
      mockDocument.getElementById.mockReturnValue(existingScript);

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

      mockDocument.getElementById.mockReturnValue(null);
      mockRenderer.createElement.mockReturnValue({
        setAttribute: vi.fn().mockName('HTMLScriptElement.setAttribute')
      });

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

      const windowWithoutGoogle = {
        toString: vi.fn().mockName('Window.toString')
      };
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => windowWithoutGoogle
      });

      mockDocument.getElementById.mockReturnValue(null);
      mockRenderer.createElement.mockReturnValue({
        setAttribute: vi.fn().mockName('HTMLScriptElement.setAttribute')
      });

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
      mockDocument.getElementById.mockReturnValue(null);
      mockRenderer.createElement.mockReturnValue(mockScript);

      vi.spyOn(GoogleSearchBarUtils, 'addCustomClassToSearchBar');

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

      mockDocument.getElementById.mockReturnValue(null);
      mockRenderer.createElement.mockReturnValue(mockScript);

      vi.spyOn(GoogleSearchBarUtils, 'addCustomClassToSearchBar');

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

      const mockSearchBox1 = {
        classList: vi.fn().mockName('HTMLElement.classList')
      };
      const mockSearchBox2 = {
        classList: vi.fn().mockName('HTMLElement.classList')
      };
      const mockSearchBoxList = [mockSearchBox1, mockSearchBox2];

      mockDocument.querySelectorAll.mockReturnValue(mockSearchBoxList as any);

      GoogleSearchBarUtils.addCustomClassToSearchBar(
        mockRenderer,
        mockDocument
      );

      expect(window.setTimeout).toHaveBeenCalledWith(
        expect.any(Function),
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

      mockDocument.querySelectorAll.mockReturnValue([] as any);

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
