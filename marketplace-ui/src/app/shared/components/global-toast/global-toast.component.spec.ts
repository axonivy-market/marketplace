import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { Subject } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { GlobalToastComponent } from './global-toast.component';
import { HttpToastService, type HttpErrorEvent } from '../../../core/services/browser/http-toast.service';

describe('GlobalToastComponent', () => {
  let fixture: ComponentFixture<GlobalToastComponent>;
  let component: GlobalToastComponent;
  let error$: Subject<HttpErrorEvent>;
  let clear$: Subject<void>;

  const setupComponent = (platformId: 'browser' | 'server' = 'browser') => {
    error$ = new Subject<HttpErrorEvent>();
    clear$ = new Subject<void>();

    const toastServiceMock = {
      getError: vi.fn(() => error$.asObservable()),
      getClear: vi.fn(() => clear$.asObservable())
    } as unknown as HttpToastService;

    TestBed.configureTestingModule({
      imports: [GlobalToastComponent, TranslateModule.forRoot()],
      providers: [
        { provide: PLATFORM_ID, useValue: platformId },
        { provide: HttpToastService, useValue: toastServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(GlobalToastComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    return { toastServiceMock };
  };

  beforeEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    setupComponent();
    expect(component).toBeTruthy();
  });

  it('should show toast when an error event is published', () => {
    setupComponent('browser');

    const payload: HttpErrorEvent = {
      status: 500,
      messageKey: 'common.error.description.500',
      timestamp: Date.now(),
      url: '/api/test'
    };

    error$.next(payload);

    expect(component.currentError).toEqual(payload);
    expect(component.isVisible).toBe(true);
  });

  it('should dismiss when clear signal is published', () => {
    setupComponent('browser');

    error$.next({
      status: 404,
      messageKey: 'common.error.description.404',
      timestamp: Date.now(),
      url: '/missing'
    });
    expect(component.isVisible).toBe(true);

    clear$.next();

    expect(component.isVisible).toBe(false);
    expect(component.currentError).toBeNull();
  });

  it('should not subscribe to toast streams on server platform', () => {
    const { toastServiceMock } = setupComponent('server');

    expect(toastServiceMock.getError as unknown as ReturnType<typeof vi.fn>).not.toHaveBeenCalled();
    expect(toastServiceMock.getClear as unknown as ReturnType<typeof vi.fn>).not.toHaveBeenCalled();
    expect(component.currentError).toBeNull();
    expect(component.isVisible).toBe(false);
  });

  it('should stop reacting to streams after destroy', () => {
    setupComponent('browser');

    component.ngOnDestroy();

    error$.next({
      status: 500,
      messageKey: 'common.error.description.500',
      timestamp: Date.now()
    });

    expect(component.currentError).toBeNull();
    expect(component.isVisible).toBe(false);
  });

  it('dismiss should hide and clear current error', () => {
    setupComponent('browser');
    component.currentError = {
      status: 400,
      messageKey: 'common.error.description.badRequest',
      timestamp: Date.now()
    };
    component.isVisible = true;

    component.dismiss();

    expect(component.isVisible).toBe(false);
    expect(component.currentError).toBeNull();
  });
});
