import 'zone.js';
import 'zone.js/testing';
import '@analogjs/vitest-angular/setup-zone';
import { getTestBed, ComponentFixture } from '@angular/core/testing';
import {
  platformBrowserTesting,
  BrowserTestingModule
} from '@angular/platform-browser/testing';
import { vi, expect } from 'vitest';

type SpyObj<T> = T & { [K in keyof T]: T[K] extends (...args: any[]) => any ? ReturnType<typeof vi.fn> : T[K] };
type Spy = ReturnType<typeof vi.fn> & { and: Record<string, unknown>; calls: Record<string, unknown> };

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
// Jasmine-specific matchers not in Vitest (toBeTrue, toBeFalse, toHaveBeenCalledOnceWith)
// ---------------------------------------------------------------------------
expect.extend({
  toBeTrue(received: unknown) {
    const pass = received === true;
    return {
      pass,
      message: () =>
        pass ? `expected ${received} not to be true` : `expected ${received} to be true`
    };
  },
  toBeFalse(received: unknown) {
    const pass = received === false;
    return {
      pass,
      message: () =>
        pass ? `expected ${received} not to be false` : `expected ${received} to be false`
    };
  },
  toHaveBeenCalledOnceWith(received: ReturnType<typeof vi.fn>, ...expected: unknown[]) {
    const calls = received.mock?.calls ?? [];
    const pass = calls.length === 1 && JSON.stringify(calls[0]) === JSON.stringify(expected);
    return {
      pass,
      message: () =>
        pass
          ? `expected mock not to have been called once with ${JSON.stringify(expected)}`
          : `expected mock to have been called once with ${JSON.stringify(expected)}, called ${calls.length} time(s): ${JSON.stringify(calls)}`
    };
  }
});

// ---------------------------------------------------------------------------
// Jasmine compatibility shim — backs each spy with vi.fn()
// ---------------------------------------------------------------------------
function makeSpyFn(name?: string) {
  const fn = vi.fn() as ReturnType<typeof vi.fn> & { and: Record<string, unknown>; calls: Record<string, unknown>; withArgs: unknown };
  const and: Record<string, unknown> = {
    returnValue: (val: unknown) => { fn.mockReturnValue(val); return fn; },
    returnValues: (...vals: unknown[]) => { vals.forEach(v => fn.mockReturnValueOnce(v)); return fn; },
    callFake: (impl: (...a: unknown[]) => unknown) => { fn.mockImplementation(impl); return fn; },
    callThrough: () => fn,
    throwError: (err: unknown) => {
      fn.mockImplementation(() => { throw (err instanceof Error ? err : new Error(String(err))); });
      return fn;
    },
    stub: () => { fn.mockReset(); return fn; }
  };
  const calls = {
    count: () => fn.mock.calls.length,
    any: () => fn.mock.calls.length > 0,
    reset: () => fn.mockClear(),
    all: () => fn.mock.calls.map((args: unknown[]) => ({ args, object: undefined, returnValue: undefined })),
    mostRecent: () => { const c = fn.mock.calls; return c.length ? { args: c.at(-1)!, object: undefined } : null; },
    first: () => { const c = fn.mock.calls; return c.length ? { args: c[0], object: undefined } : null; },
    argsFor: (i: number) => fn.mock.calls[i] ?? []
  };
  (fn as unknown as Record<string, unknown>)['and'] = and;
  (fn as unknown as Record<string, unknown>)['calls'] = calls;
  (fn as unknown as Record<string, unknown>)['withArgs'] = () => fn;
  if (name) (fn as unknown as Record<string, unknown>)['and.identity'] = name;
  return fn;
}

function createSpyObj<T = object>(
  baseName: string | Record<string, unknown>,
  methods: string[] | Record<string, unknown>,
  accessors?: Record<string, unknown>
): SpyObj<T> {
  const obj: Record<string, unknown> = {};
  const methodNames = Array.isArray(methods) ? methods : Object.keys(methods);
  methodNames.forEach(method => {
    const spy = makeSpyFn(`${typeof baseName === 'string' ? baseName : ''}#${method}`);
    if (!Array.isArray(methods)) {
      const val = methods[method];
      if (val !== undefined) (spy as unknown as { and: { returnValue(v: unknown): void } }).and.returnValue(val);
    }
    obj[method] = spy;
  });
  if (accessors) {
    Object.entries(accessors).forEach(([prop, value]) => {
      Object.defineProperty(obj, prop, { get: vi.fn().mockReturnValue(value), configurable: true });
    });
  }
  return obj as SpyObj<T>;
}

function spyOn<T extends object, K extends keyof T>(obj: T, method: K): Spy {
  const original = obj[method];
  const spy = makeSpyFn(String(method));
  if (typeof original === 'function') {
    (spy as unknown as { and: { callThrough: () => void } }).and.callThrough();
    spy.mockImplementation((...args: unknown[]) => (original as (...a: unknown[]) => unknown).apply(obj, args));
  }
  obj[method] = spy as unknown as T[K];
  return spy as unknown as Spy;
}

(globalThis as Record<string, unknown>)['jasmine'] = {
  createSpyObj,
  createSpy: makeSpyFn,
  any: (ctor: new (...a: unknown[]) => unknown) => expect.any(ctor),
  anything: () => expect.anything(),
  objectContaining: (s: Record<string, unknown>) => expect.objectContaining(s),
  arrayContaining: (s: unknown[]) => expect.arrayContaining(s),
  stringContaining: (s: string) => expect.stringContaining(s),
  stringMatching: (s: string | RegExp) => expect.stringMatching(s),
  clock: (() => {
    const clock = {
      install: () => { vi.useFakeTimers(); return clock; },
      uninstall: () => { vi.useRealTimers(); return clock; },
      tick: (ms: number) => { vi.advanceTimersByTime(ms); return clock; },
      mockDate: (date?: Date) => { vi.setSystemTime(date ?? new Date()); return clock; }
    };
    return () => clock;
  })()
};

(globalThis as Record<string, unknown>)['spyOn'] = spyOn;

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

// karma-viewport compatibility shim
(globalThis as Record<string, unknown>)['viewport'] = {
  set: (width: number, height?: number) => {
    Object.defineProperty(globalThis, 'innerWidth', { writable: true, configurable: true, value: width });
    if (height !== undefined)
      Object.defineProperty(globalThis, 'innerHeight', { writable: true, configurable: true, value: height });
    globalThis.dispatchEvent(new Event('resize'));
  },
  reset: () => {
    Object.defineProperty(globalThis, 'innerWidth', { writable: true, configurable: true, value: 1024 });
    Object.defineProperty(globalThis, 'innerHeight', { writable: true, configurable: true, value: 768 });
    globalThis.dispatchEvent(new Event('resize'));
  }
};

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
