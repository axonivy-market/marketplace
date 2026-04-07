import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarkdownEditorComponent } from './markdown-editor.component';
import { PLATFORM_ID, signal, WritableSignal } from '@angular/core';
import { vi, describe, beforeEach, expect, it } from 'vitest';

vi.mock('easymde', () => {
  class FakeCodeMirrorInner {
    private changeHandler?: () => void;
    setOption() {}
    getCursor() { return { line: 0, ch: 0 }; }
    setCursor() {}
    getWrapperElement() {
      const container = document.createElement('div');
      container.classList.add('EasyMDEContainer');
      const toolbar = document.createElement('div');
      toolbar.classList.add('editor-toolbar');
      const cm = document.createElement('div');
      cm.classList.add('CodeMirror');
      container.appendChild(toolbar);
      container.appendChild(cm);
      return { closest: () => container } as any;
    }
    on(event: string, cb: () => void) {
      if (event === 'change') { this.changeHandler = cb; }
    }
    triggerChange() { this.changeHandler?.(); }
  }
  class FakeEasyMDEInner {
    codemirror = new FakeCodeMirrorInner();
    private _value = '';
    constructor(public config: any) {}
    value(val?: string): any {
      if (val !== undefined) { this._value = val; }
      return this._value;
    }
    toTextArea() {}
    cleanup() {}
  }
  return { default: FakeEasyMDEInner };
});

class FakeCodeMirror {
  private changeHandler?: () => void;

  setOption() {}

  getCursor() {
    return { line: 0, ch: 0 };
  }

  setCursor() {}

  getWrapperElement() {
    const container = document.createElement('div');
    container.classList.add('EasyMDEContainer');

    const toolbar = document.createElement('div');
    toolbar.classList.add('editor-toolbar');

    const cm = document.createElement('div');
    cm.classList.add('CodeMirror');

    container.appendChild(toolbar);
    container.appendChild(cm);

    return {
      closest: () => container
    } as any;
  }

  on(event: string, cb: () => void) {
    if (event === 'change') {
      this.changeHandler = cb;
    }
  }

  triggerChange() {
    this.changeHandler?.();
  }
}

class FakeEasyMDE {
  codemirror = new FakeCodeMirror();
  private _value = '';

  constructor(public config: any) {}

  value(val?: string): any {
    if (val !== undefined) {
      this._value = val;
    }
    return this._value;
  }

  toTextArea() {}
  cleanup() {}
}

describe('MarkdownEditorComponent', () => {
  let component: MarkdownEditorComponent;
  let fixture: ComponentFixture<MarkdownEditorComponent>;
  let submittingSignal: WritableSignal<boolean>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MarkdownEditorComponent],
      providers: [{ provide: PLATFORM_ID, useValue: 'browser' }]
    }).compileComponents();

    fixture = TestBed.createComponent(MarkdownEditorComponent);
    component = fixture.componentInstance;

    component.autosaveId = 'test-id';
    submittingSignal = signal(false);
    fixture.componentRef.setInput('isSubmittingSignal', submittingSignal);

    fixture.detectChanges();
  });

  function mockDynamicImport() {
    vi.spyOn<any, any>(globalThis, 'import' as any).mockImplementation((module: unknown) => {
      const mod = module as string;
      if (module === 'easymde') {
        return Promise.resolve({ default: FakeEasyMDE });
      }
      return Promise.reject(module);
    });
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize EasyMDE', async () => {
    vi.spyOn(component as any, 'loadEasyMDE').mockResolvedValue({
      default: FakeEasyMDE
    });

    await component.initializeEditor();

    expect(component['mde']).toBeTruthy();
    expect(component.isMDEReady).toBe(true);
  });

  it('should update content when editor changes', async () => {
    vi.spyOn(component as any, 'loadEasyMDE').mockResolvedValue({
      default: FakeEasyMDE
    });

    await component.initializeEditor();

    const fakeMde = component['mde'] as any;

    fakeMde.value('new value');
    fakeMde.codemirror.triggerChange();

    expect(component.contentValue()).toBe('new value');
  });

  it('should toggle readOnly when submitting changes', async () => {
    vi.spyOn(component as any, 'loadEasyMDE').mockResolvedValue({
      default: FakeEasyMDE
    });

    await component.initializeEditor();

    const fakeMde = component['mde'] as any;
    const setOptionSpy = vi.spyOn(fakeMde.codemirror, 'setOption');

    submittingSignal.set(true);
    fixture.detectChanges();

    expect(setOptionSpy).toHaveBeenCalledWith('readOnly', 'nocursor');

    submittingSignal.set(false);
    fixture.detectChanges();

    expect(setOptionSpy).toHaveBeenCalledWith('readOnly', false);
  });

  it('should cleanup on destroy', async () => {
    vi.spyOn(component as any, 'loadEasyMDE').mockResolvedValue({
      default: FakeEasyMDE
    });

    await component.initializeEditor();

    const fakeMde = component['mde'] as any;

    const toTextAreaSpy = vi.spyOn(fakeMde, 'toTextArea');
    const cleanupSpy = vi.spyOn(fakeMde, 'cleanup');

    component.ngOnDestroy();

    expect(toTextAreaSpy).toHaveBeenCalled();
    expect(cleanupSpy).toHaveBeenCalled();
  });

  it('should sync editor value and preserve cursor when contentValue changes', async () => {
    vi.spyOn(component as any, 'loadEasyMDE').mockResolvedValue({
      default: FakeEasyMDE
    });

    await component.initializeEditor();

    const fakeMde = component['mde'] as any;
    const cm = fakeMde.codemirror;

    fakeMde.value('old');

    const getCursorSpy = vi.spyOn(cm, 'getCursor').mockReturnValue({
      line: 3,
      ch: 5
    });

    const setCursorSpy = vi.spyOn(cm, 'setCursor');
    const valueSpy = vi.spyOn(fakeMde, 'value');

    component.contentValue.set('new');

    fixture.detectChanges();

    expect(getCursorSpy).toHaveBeenCalled();
    expect(valueSpy).toHaveBeenCalledWith('new');
    expect(setCursorSpy).toHaveBeenCalledWith({ line: 3, ch: 5 });
  });

  it('should NOT initialize editor when running on server platform', () => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      imports: [MarkdownEditorComponent],
      providers: [{ provide: PLATFORM_ID, useValue: 'server' }]
    }).compileComponents();

    const serverFixture = TestBed.createComponent(MarkdownEditorComponent);
    const serverComponent = serverFixture.componentInstance;

    vi.spyOn(serverComponent as any, 'initializeEditor');

    serverFixture.detectChanges();

    expect((serverComponent as any).initializeEditor).not.toHaveBeenCalled();
    expect(serverComponent['mde']).toBeUndefined();
  });

  it('should set content when mde is initialized', async () => {
    vi.spyOn(component as any, 'loadEasyMDE').mockResolvedValue({
      default: FakeEasyMDE
    });

    await component.initializeEditor();

    const fakeMde = component['mde'] as any;
    const valueSpy = vi.spyOn(fakeMde, 'value');

    component.setEasyMDEContent('hello world');

    expect(valueSpy).toHaveBeenCalledWith('hello world');
  });

  it('should NOT set content if mde is not initialized', () => {
    component['mde'] = undefined as any;

    expect(() => {
      component.setEasyMDEContent('test');
    }).not.toThrow();
  });
});
