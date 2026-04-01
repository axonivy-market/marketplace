import 'zone.js';
import 'zone.js/testing';
import { getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting
} from '@angular/platform-browser-dynamic/testing';
import { vi } from 'vitest';

// Initialize Angular's test environment once for the whole suite.
getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting(),
  { teardown: { destroyAfterEach: true } }
);

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
    const calls = (received as ReturnType<typeof vi.fn>).mock?.calls ?? [];
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
    mostRecent: () => { const c = fn.mock.calls; return c.length ? { args: c[c.length - 1], object: undefined } : null; },
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
): jasmine.SpyObj<T> {
  const obj: Record<string, unknown> = {};
  const methodNames = Array.isArray(methods) ? methods : Object.keys(methods);
  methodNames.forEach(method => {
    const spy = makeSpyFn(`${typeof baseName === 'string' ? baseName : ''}#${method}`);
    if (!Array.isArray(methods)) {
      const val = (methods as Record<string, unknown>)[method];
      if (val !== undefined) (spy as unknown as { and: { returnValue(v: unknown): void } }).and.returnValue(val);
    }
    obj[method] = spy;
  });
  if (accessors) {
    Object.entries(accessors).forEach(([prop, value]) => {
      Object.defineProperty(obj, prop, { get: vi.fn().mockReturnValue(value), configurable: true });
    });
  }
  return obj as jasmine.SpyObj<T>;
}

function spyOn<T extends object, K extends keyof T>(obj: T, method: K): jasmine.Spy {
  const original = obj[method];
  const spy = makeSpyFn(String(method));
  if (typeof original === 'function') {
    (spy as unknown as { and: { callThrough: () => void } }).and.callThrough();
    spy.mockImplementation((...args: unknown[]) => (original as (...a: unknown[]) => unknown).apply(obj, args));
  }
  obj[method] = spy as unknown as T[K];
  return spy as unknown as jasmine.Spy;
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
Object.defineProperty(window, 'matchMedia', {
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
    Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: width });
    if (height !== undefined)
      Object.defineProperty(window, 'innerHeight', { writable: true, configurable: true, value: height });
    window.dispatchEvent(new Event('resize'));
  },
  reset: () => {
    Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1024 });
    Object.defineProperty(window, 'innerHeight', { writable: true, configurable: true, value: 768 });
    window.dispatchEvent(new Event('resize'));
  }
};
