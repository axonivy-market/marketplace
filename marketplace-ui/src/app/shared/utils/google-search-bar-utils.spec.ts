import { Renderer2 } from '@angular/core';
import { WindowRef } from '../../core/services/browser/window-ref.service';
import { DocumentRef } from '../../core/services/browser/document-ref.service';
import { GoogleSearchBarUtils } from './google-search-bar.utils';
import { environment } from '../../../environments/environment';

describe('GoogleSearchBarUtils', () => {
  let mockRenderer: jasmine.SpyObj<Renderer2>;
  let mockWindowRef: jasmine.SpyObj<WindowRef>;
  let mockDocumentRef: jasmine.SpyObj<DocumentRef>;
  let mockDocument: jasmine.SpyObj<Document>;
  let mockWindow: jasmine.SpyObj<Window>;

  beforeEach(() => {
    // Create mock objects
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

    mockDocument = jasmine.createSpyObj('Document', [
      'getElementById',
      'querySelectorAll'
    ], {
      body: jasmine.createSpyObj('HTMLElement', ['appendChild'])
    });

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

    // Spy on setTimeout
    spyOn(globalThis, 'setTimeout').and.callFake(((fn: Function) => {
    fn();
    return 1;
    }) as any);
  });

  describe('renderGoogleSearchBar', () => {
    it('should return early if document is not available', () => {
      // Arrange
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => undefined
      });

      // Act
      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      // Assert
      expect(mockRenderer.createElement).not.toHaveBeenCalled();
    });

    it('should create and append script element when googleCSEScript does not exist', () => {
      // Arrange
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => mockWindow
      });

      const mockScript = jasmine.createSpyObj('HTMLScriptElement', ['setAttribute'], {
        onload: undefined
      });
      
      mockDocument.getElementById.and.returnValue(null);
      mockRenderer.createElement.and.returnValue(mockScript);

      // Act
      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      // Assert
      expect(mockDocument.getElementById).toHaveBeenCalledWith('googleCSEScript');
      expect(mockRenderer.createElement).toHaveBeenCalledWith('script');
      expect(mockRenderer.appendChild).toHaveBeenCalledWith(mockDocument.body, mockScript);
      
      // Verify script properties are set
      expect(mockScript.id).toBe(environment.googleProgrammableSearchScriptId);
      expect(mockScript.type).toBe(environment.googleProgrammableSearchScriptType);
      expect(mockScript.async).toBe(true);
      expect(mockScript.src).toBe(environment.googleProgrammableSearchScriptSource);
    });

    it('should not create script element when googleCSEScript already exists', () => {
      // Arrange
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => mockWindow
      });

      const existingScript = jasmine.createSpyObj('HTMLScriptElement', ['getAttribute']);
      mockDocument.getElementById.and.returnValue(existingScript);

      // Act
      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      // Assert
      expect(mockRenderer.createElement).not.toHaveBeenCalled();
      expect(mockRenderer.appendChild).not.toHaveBeenCalled();
    });

    it('should call google.search.cse.element.render when google search is available', () => {
      // Arrange
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => mockWindow
      });

      mockDocument.getElementById.and.returnValue(null);
      mockRenderer.createElement.and.returnValue(jasmine.createSpyObj('HTMLScriptElement', ['setAttribute']));

      // Act
      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      // Assert
      expect(mockWindow.google.search.cse.element.render).toHaveBeenCalledWith('gcse-search');
    });

    it('should not call google.search.cse.element.render when google search is not available', () => {
      // Arrange
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });
      
      const windowWithoutGoogle = jasmine.createSpyObj('Window', ['toString']);
      Object.defineProperty(mockWindowRef, 'nativeWindow', {
        get: () => windowWithoutGoogle
      });

      mockDocument.getElementById.and.returnValue(null);
      mockRenderer.createElement.and.returnValue(jasmine.createSpyObj('HTMLScriptElement', ['setAttribute']));

      // Act
      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      // Assert - Should not throw error even when google is not available
      expect(() => {
        GoogleSearchBarUtils.renderGoogleSearchBar(
          mockRenderer,
          mockWindowRef,
          mockDocumentRef
        );
      }).not.toThrow();
    });

    it('should trigger addCustomClassToSearchBar when script loads', () => {
      // Arrange
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

      // Act
      GoogleSearchBarUtils.renderGoogleSearchBar(
        mockRenderer,
        mockWindowRef,
        mockDocumentRef
      );

      // Trigger the onload event
      mockScript.onload();

      // Assert
      expect(GoogleSearchBarUtils.addCustomClassToSearchBar).toHaveBeenCalledWith(
        mockRenderer,
        mockDocumentRef
      );
    });
  });

  describe('addCustomClassToSearchBar', () => {
    it('should return early if document is not available', () => {
      // Arrange
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => undefined
      });

      // Act
      GoogleSearchBarUtils.addCustomClassToSearchBar(mockRenderer, mockDocumentRef);

      // Assert
      expect(window.setTimeout).not.toHaveBeenCalled();
    });

    it('should add custom class to search bar elements after timeout', () => {
      // Arrange
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });

      const mockSearchBox1 = jasmine.createSpyObj('HTMLElement', ['classList']);
      const mockSearchBox2 = jasmine.createSpyObj('HTMLElement', ['classList']);
      const mockSearchBoxList = [mockSearchBox1, mockSearchBox2];

      mockDocument.querySelectorAll.and.returnValue(mockSearchBoxList as any);

      // Act
      GoogleSearchBarUtils.addCustomClassToSearchBar(mockRenderer, mockDocumentRef);

      // Assert
      expect(window.setTimeout).toHaveBeenCalledWith(jasmine.any(Function), 1000);
      expect(mockDocument.querySelectorAll).toHaveBeenCalledWith('.gsc-control-cse');
      expect(mockRenderer.addClass).toHaveBeenCalledWith(mockSearchBox1, 'bg-secondary');
      expect(mockRenderer.addClass).toHaveBeenCalledWith(mockSearchBox2, 'bg-secondary');
    });

    it('should handle empty search box list gracefully', () => {
      // Arrange
      Object.defineProperty(mockDocumentRef, 'nativeDocument', {
        get: () => mockDocument
      });

      mockDocument.querySelectorAll.and.returnValue([] as any);

      // Act
      GoogleSearchBarUtils.addCustomClassToSearchBar(mockRenderer, mockDocumentRef);

      // Assert
      expect(mockDocument.querySelectorAll).toHaveBeenCalledWith('.gsc-control-cse');
      expect(mockRenderer.addClass).not.toHaveBeenCalled();
    });
  });
});