import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarkdownEditorComponent } from './markdown-editor.component';
import { PLATFORM_ID, signal, WritableSignal } from '@angular/core';

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
    spyOn<any>(window, 'import').and.callFake((module: string) => {
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
    spyOn(component as any, 'loadEasyMDE').and.resolveTo({
      default: FakeEasyMDE
    });

    await component.ngAfterViewInit();

    expect(component['mde']).toBeTruthy();
    expect(component.isMDEReady).toBeTrue();
  });

  it('should update content when editor changes', async () => {
    spyOn(component as any, 'loadEasyMDE').and.resolveTo({
      default: FakeEasyMDE
    });

    await component.ngAfterViewInit();

    const fakeMde = component['mde'] as any;

    fakeMde.value('new value');
    fakeMde.codemirror.triggerChange();

    expect(component.contentValue()).toBe('new value');
  });

  it('should toggle readOnly when submitting changes', async () => {
    spyOn(component as any, 'loadEasyMDE').and.resolveTo({
      default: FakeEasyMDE
    });

    await component.ngAfterViewInit();

    const fakeMde = component['mde'] as any;
    const setOptionSpy = spyOn(fakeMde.codemirror, 'setOption');

    submittingSignal.set(true);
    fixture.detectChanges();

    expect(setOptionSpy).toHaveBeenCalledWith('readOnly', 'nocursor');

    submittingSignal.set(false);
    fixture.detectChanges();

    expect(setOptionSpy).toHaveBeenCalledWith('readOnly', false);
  });

  it('should cleanup on destroy', async () => {
    spyOn(component as any, 'loadEasyMDE').and.resolveTo({
      default: FakeEasyMDE
    });

    await component.ngAfterViewInit();

    const fakeMde = component['mde'] as any;

    const toTextAreaSpy = spyOn(fakeMde, 'toTextArea');
    const cleanupSpy = spyOn(fakeMde, 'cleanup');

    component.ngOnDestroy();

    expect(toTextAreaSpy).toHaveBeenCalled();
    expect(cleanupSpy).toHaveBeenCalled();
  });
});
