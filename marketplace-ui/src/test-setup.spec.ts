import { describe, it, expect, vi, beforeEach } from 'vitest';

// ── jsdom shims ─────────────────────────────────────────────────────────────

describe('matchMedia shim', () => {
  it('returns a media-query-like object', () => {
    const result = globalThis.matchMedia('(max-width: 768px)');
    expect(result.matches).toBe(false);
    expect(result.media).toBe('(max-width: 768px)');
    expect(typeof result.addEventListener).toBe('function');
  });
});

describe('ResizeObserver shim', () => {
  it('is defined globally', () => {
    expect(globalThis.ResizeObserver).toBeDefined();
  });

  it('exposes observe, unobserve and disconnect as functions', () => {
    const ro = new globalThis.ResizeObserver(() => {});
    expect(typeof ro.observe).toBe('function');
    expect(typeof ro.unobserve).toBe('function');
    expect(typeof ro.disconnect).toBe('function');
  });
});

describe('IntersectionObserver shim', () => {
  it('is defined globally', () => {
    expect(globalThis.IntersectionObserver).toBeDefined();
  });

  it('exposes observe, unobserve and disconnect as functions', () => {
    const io = new (globalThis as any).IntersectionObserver();
    expect(typeof io.observe).toBe('function');
    expect(typeof io.unobserve).toBe('function');
    expect(typeof io.disconnect).toBe('function');
  });
});

describe('sessionStorage mock', () => {
  beforeEach(() => globalThis.sessionStorage.clear());

  it('setItem stores a value retrievable by getItem', () => {
    globalThis.sessionStorage.setItem('key', 'value');
    expect(globalThis.sessionStorage.getItem('key')).toBe('value');
  });

  it('getItem returns null for unknown keys', () => {
    expect(globalThis.sessionStorage.getItem('missing')).toBeNull();
  });

  it('removeItem deletes a stored key', () => {
    globalThis.sessionStorage.setItem('k', 'v');
    globalThis.sessionStorage.removeItem('k');
    expect(globalThis.sessionStorage.getItem('k')).toBeNull();
  });

  it('clear removes all entries', () => {
    globalThis.sessionStorage.setItem('a', '1');
    globalThis.sessionStorage.setItem('b', '2');
    globalThis.sessionStorage.clear();
    expect(globalThis.sessionStorage.length).toBe(0);
  });

  it('length reflects the number of stored items', () => {
    globalThis.sessionStorage.setItem('x', '1');
    expect(globalThis.sessionStorage.length).toBe(1);
  });

  it('key returns the nth stored key', () => {
    globalThis.sessionStorage.setItem('only', '1');
    expect(globalThis.sessionStorage.key(0)).toBe('only');
    expect(globalThis.sessionStorage.key(99)).toBeNull();
  });
});

describe('localStorage mock', () => {
  beforeEach(() => globalThis.localStorage.clear());

  it('setItem and getItem work correctly', () => {
    globalThis.localStorage.setItem('foo', 'bar');
    expect(globalThis.localStorage.getItem('foo')).toBe('bar');
  });

  it('removeItem deletes the entry', () => {
    globalThis.localStorage.setItem('foo', 'bar');
    globalThis.localStorage.removeItem('foo');
    expect(globalThis.localStorage.getItem('foo')).toBeNull();
  });
});

describe('scrollTo shim', () => {
  it('is a function', () => {
    expect(typeof globalThis.scrollTo).toBe('function');
  });
});

describe('window.open shim', () => {
  it('is a function', () => {
    expect(typeof globalThis.open).toBe('function');
  });
});

describe('DataTransfer shim', () => {
  it('files is initially empty', () => {
    const dt = new (globalThis as any).DataTransfer();
    expect(dt.files.length).toBe(0);
  });

  it('items.add() makes a file accessible via files', () => {
    const dt = new (globalThis as any).DataTransfer();
    const file = new File(['content'], 'test.txt', { type: 'text/plain' });
    dt.items.add(file);
    expect(dt.files.length).toBe(1);
    expect(dt.files.item(0)).toBe(file);
  });

  it('items.remove() removes a file by index', () => {
    const dt = new (globalThis as any).DataTransfer();
    const file = new File([''], 'a.txt');
    dt.items.add(file);
    dt.items.remove(0);
    expect(dt.files.length).toBe(0);
  });

  it('items.clear() removes all files', () => {
    const dt = new (globalThis as any).DataTransfer();
    dt.items.add(new File([''], 'a.txt'));
    dt.items.add(new File([''], 'b.txt'));
    dt.items.clear();
    expect(dt.files.length).toBe(0);
  });

  it('getData returns empty string', () => {
    const dt = new (globalThis as any).DataTransfer();
    expect(dt.getData('text/plain')).toBe('');
  });

  it('files is iterable', () => {
    const dt = new (globalThis as any).DataTransfer();
    const file = new File([''], 'c.txt');
    dt.items.add(file);
    const collected = [...dt.files];
    expect(collected).toContain(file);
  });
});

describe('DragEvent shim', () => {
  it('creates an event with the correct type', () => {
    const dt = new (globalThis as any).DataTransfer();
    const event = new (globalThis as any).DragEvent('drop', { dataTransfer: dt });
    expect(event.type).toBe('drop');
    expect(event.dataTransfer).toBe(dt);
  });

  it('dataTransfer is null when not provided', () => {
    const event = new (globalThis as any).DragEvent('dragstart');
    expect(event.dataTransfer).toBeNull();
  });
});
