import 'zone.js';
import '@analogjs/vitest-angular/setup-zone';
import { getTestBed, ComponentFixture } from '@angular/core/testing';
import {
  platformBrowserTesting,
  BrowserTestingModule
} from '@angular/platform-browser/testing';
import { vi } from 'vitest';

// Initialize Angular's test environment once for the whole suite.
// Wrapped in try/catch because `ng test` (via @angular/build:unit-test) initializes
// TestBed internally before this file runs, causing a "already been called" error.
try {
  getTestBed().initTestEnvironment(
    BrowserTestingModule,
    platformBrowserTesting(),
    { teardown: { destroyAfterEach: true } }
  );
} catch {
  // Already initialized by the Angular CLI builder — safe to ignore.
}

// ---------------------------------------------------------------------------
// jsdom missing API shims
// ---------------------------------------------------------------------------
Object.defineProperty(globalThis, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn()
  }))
});

// ResizeObserver shim — not implemented in jsdom
class ResizeObserverStub {
  observe = vi.fn();
  unobserve = vi.fn();
  disconnect = vi.fn();
}
Object.defineProperty(globalThis, 'ResizeObserver', { writable: true, configurable: true, value: ResizeObserverStub });

// IntersectionObserver shim — not implemented in jsdom
class IntersectionObserverStub {
  observe = vi.fn();
  unobserve = vi.fn();
  disconnect = vi.fn();
}
Object.defineProperty(globalThis, 'IntersectionObserver', { writable: true, configurable: true, value: IntersectionObserverStub });

// scrollIntoView shim — not implemented in jsdom
if (!Element.prototype.scrollIntoView) {
  Element.prototype.scrollIntoView = vi.fn();
}

// Replace sessionStorage/localStorage with vi.fn()-backed mocks so
// vi.spyOn() and toHaveBeenCalledWith() work correctly on them.
function makeStorageMock() {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] ?? null),
    setItem: vi.fn((key: string, value: string) => { store[key] = String(value); }),
    removeItem: vi.fn((key: string) => { delete store[key]; }),
    clear: vi.fn(() => { store = {}; }),
    key: vi.fn((index: number) => Object.keys(store)[index] ?? null),
    get length() { return Object.keys(store).length; }
  };
}

Object.defineProperty(globalThis, 'sessionStorage', { value: makeStorageMock(), writable: true });
Object.defineProperty(globalThis, 'localStorage', { value: makeStorageMock(), writable: true });

// scrollTo is not implemented in jsdom
globalThis.scrollTo = vi.fn() as unknown as typeof globalThis.scrollTo;

// focus is not implemented in jsdom
window.focus = vi.fn();

// open is not implemented in jsdom  
globalThis.open = vi.fn() as unknown as typeof globalThis.open;

// ---------------------------------------------------------------------------
// Always define DataTransfer shim (jsdom doesn't implement it)
(globalThis as Record<string, unknown>)['DataTransfer'] = class DataTransfer {
  private _files: File[] = [];
  items: DataTransferItemList;
  get files(): FileList {
    const files = this._files;
    const fileList = {
      length: files.length, item: (i: number) => files[i] ?? null, [Symbol.iterator]: function*() { yield* files; },
      ...Object.fromEntries(files.map((f, i) => [i, f]))
    };
    return fileList as unknown as FileList;
  }
  constructor() {
    const getFiles = () => this._files;
    const setFiles = (f: File[]) => { this._files = f; };
    this.items = {
      get length() { return getFiles().length; },
      add(file: File | string, _type?: string) {
        if (file instanceof File) getFiles().push(file);
      },
      clear() { setFiles([]); },
      remove(i: number) { getFiles().splice(i, 1); },
      [Symbol.iterator]: function*() { yield* getFiles(); }
    } as unknown as DataTransferItemList;
  }
  getData(_format: string): string { return ''; }
  setData(_format: string, _data: string): void { /* noop */ }
  clearData(): void { /* noop */ }
  dropEffect: string = 'none';
  effectAllowed: string = 'all';
  types: string[] = [];
};
// Always define DragEvent shim (jsdom doesn't implement it)
(globalThis as Record<string, unknown>)['DragEvent'] = class DragEvent extends MouseEvent {
  dataTransfer: DataTransfer | null;
  constructor(type: string, init?: DragEventInit) {
    super(type, init);
    this.dataTransfer = (init as any)?.dataTransfer ?? null;
  }
};

// ---------------------------------------------------------------------------
// Match Karma's behavior: ComponentFixture.detectChanges() defaults to
// checkNoChanges=false. Karma swallowed NG0100 errors; this makes Vitest
// behave the same way so pre-existing tests don't suddenly start failing.
// ---------------------------------------------------------------------------
const _origDetectChanges = ComponentFixture.prototype.detectChanges;
ComponentFixture.prototype.detectChanges = function(checkNoChanges = false) {
  return _origDetectChanges.call(this, checkNoChanges);
};
