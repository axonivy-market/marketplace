import { vi, describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewsComponent } from './news.component';
import { ElementRef, PLATFORM_ID } from '@angular/core';
import { AdminDashboardService } from '../admin-dashboard/admin-dashboard.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { PageTitleService } from '../../shared/services/page-title.service';
import { MarkdownService } from '../../shared/services/markdown.service';
import { LoadingService } from '../../core/services/loading/loading.service';
import { of, throwError } from 'rxjs';

describe('NewsComponent', () => {
  let component: NewsComponent;
  let fixture: ComponentFixture<NewsComponent>;

  let mockAdminDashboardService: any;
  let mockPageTitleService: any;
  let mockMarkdownService: any;

  beforeEach(async () => {
    mockAdminDashboardService = {
      getReleaseLetters: vi.fn().mockReturnValue(
        of({
          _embedded: { releaseLetterModelList: [] },
          _links: {},
          page: { number: 0, totalPages: 0 }
        })
      )
    };

    mockPageTitleService = {
      setTitleOnLangChange: vi.fn()
    };

    mockMarkdownService = {
      parseMarkdown: vi.fn().mockImplementation((c: string) => `<p>${c}</p>`)
    };

    await TestBed.configureTestingModule({
      imports: [NewsComponent, TranslateModule.forRoot()],
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: AdminDashboardService, useValue: mockAdminDashboardService },
        TranslateService,
        { provide: PageTitleService, useValue: mockPageTitleService },
        { provide: MarkdownService, useValue: mockMarkdownService },
        LoadingService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NewsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should convert api response list to safe html model list', () => {
    const items = [
      {
        sprint: 'S1',
        content: 'hello',
        latest: true,
        createdAt: '2026-02-01'
      }
    ] as any;

    const result = component.toSafeHtmlModelList(items);

    expect(result.length).toBe(1);
    expect(result[0].sprint).toBe('S1');
    expect(mockMarkdownService.parseMarkdown).toHaveBeenCalledWith('hello');
  });

  it('should convert single api response to safe html model', () => {
    const item = {
      sprint: 'S42',
      content: 'markdown',
      latest: false,
      createdAt: '2026-02-02'
    } as any;

    const result = component.toSafeHtmlModel(item);

    expect(result.sprint).toBe('S42');
    expect(result.latest).toBe(false);
    expect(result.createdAt).toBe('2026-02-02');
    expect(mockMarkdownService.parseMarkdown).toHaveBeenCalledWith('markdown');
  });

  it('should render markdown and sanitize html', () => {
    const result = component.renderReleaseLetterContent('abc');

    expect(mockMarkdownService.parseMarkdown).toHaveBeenCalledWith('abc');
    expect(result).toBeTruthy();
  });

  it('should return translated sprint title', () => {
    vi.spyOn(component.translateService, 'instant').mockReturnValue('Sprint ');

    const result = component.getSprintTitle('S100');

    expect(component.translateService.instant).toHaveBeenCalledWith(
      'common.admin.news.sprint'
    );
    expect(result).toBe('Sprint S100');
  });

  it('should return false if no links or pages', () => {
    component.newsLinks = undefined as any;
    component.newsPages = undefined as any;

    expect(component.hasMoreReleaseLetters()).toBe(false);
  });

  it('should return true if next page exists', () => {
    component.newsPages = { number: 0, totalPages: 2 } as any;
    component.newsLinks = {
      next: { href: 'next-page-url' }
    } as any;

    expect(component.hasMoreReleaseLetters()).toBe(true);
  });

  it('should return false if already at last page', () => {
    component.newsPages = { number: 2, totalPages: 2 } as any;
    component.newsLinks = {} as any;

    expect(component.hasMoreReleaseLetters()).toBe(false);
  });

  it('should set empty title when no release letters returned', () => {
    vi.spyOn(component.translateService, 'instant').mockReturnValue(
      'No releases'
    );

    component.loadReleaseLetters();

    expect(component.emptyReleaseLetterTitle).toBe('No releases');
  });

  it('should append release letters when response contains data', () => {
    const response = {
      _embedded: {
        releaseLetterModelList: [
          {
            sprint: 'S1',
            content: 'test',
            latest: true,
            createdAt: '2026-02-01'
          }
        ]
      },
      _links: {
        self: { href: 'self-url' },
        next: { href: 'next-url' }
      },
      page: {
        size: 10,
        totalElements: 1,
        totalPages: 2,
        number: 0
      }
    };

    mockAdminDashboardService.getReleaseLetters.mockReturnValue(of(response));

    component.loadReleaseLetters();

    const list = component.releaseLetterSafeHtmlContentList();

    expect(list.length).toBe(1);
    expect(component.newsLinks).toEqual(response._links);
    expect(component.newsPages).toEqual(response.page);
    expect(component.releaseLetterCriteria.nextPageHref).toBe('next-url');
  });

  it('should setup intersection observer and call loadReleaseLetters when intersecting', () => {
    const observeSpy = vi.fn();

    let savedCallback!: IntersectionObserverCallback;

    class MockIntersectionObserver {
      constructor(callback: IntersectionObserverCallback) {
        savedCallback = callback;
      }

      observe = observeSpy;
      unobserve() {}
      disconnect() {}
    }

    (window as any).IntersectionObserver = MockIntersectionObserver;

    vi.spyOn(component, 'hasMoreReleaseLetters').mockReturnValue(true);
    vi.spyOn(component.loadingService, 'isLoading').mockReturnValue(false);
    vi.spyOn(component, 'loadReleaseLetters');

    component.observerElement = {
      nativeElement: document.createElement('div')
    } as ElementRef;

    component.setupIntersectionObserver();

    savedCallback(
      [{ isIntersecting: true } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );

    expect(component.loadReleaseLetters).toHaveBeenCalled();
    expect(observeSpy).toHaveBeenCalled();
  });

  it('should not setup intersection observer if not running in browser', () => {
    component.isBrowser = false;

    const originalIO = window.IntersectionObserver;
    const ioSpy = vi.fn();
    (window as any).IntersectionObserver = ioSpy;

    component.setupIntersectionObserver();

    expect(ioSpy).not.toHaveBeenCalled();

    window.IntersectionObserver = originalIO;
  });

  it('should not setup intersection observer if IntersectionObserver is undefined', () => {
    component.isBrowser = true;

    const originalIO = window.IntersectionObserver;
    (window as any).IntersectionObserver = undefined;

    component.setupIntersectionObserver();
    // No assertions needed - test passes if no errors are thrown
    window.IntersectionObserver = originalIO;
  });

  it('should unsubscribe all subscriptions on destroy', () => {
    const unsubscribeSpy = vi.fn();

    component.subscriptions = [{ unsubscribe: unsubscribeSpy } as any];

    component.ngOnDestroy();

    expect(unsubscribeSpy).toHaveBeenCalled();
  });

  it('should return early if response is null', () => {
    mockAdminDashboardService.getReleaseLetters.mockReturnValue(of(null));

    const freshFixture = TestBed.createComponent(NewsComponent);
    const freshComponent = freshFixture.componentInstance;

    freshComponent.loadReleaseLetters();

    expect(freshComponent.newsLinks).toBeUndefined();
    expect(freshComponent.newsPages).toBeUndefined();
    expect(freshComponent.releaseLetterSafeHtmlContentList()).toEqual([]);
  });

  it('should handle error from getReleaseLetters', () => {
    const testError = new Error('Test error');

    mockAdminDashboardService.getReleaseLetters.mockReturnValue(
      throwError(() => testError)
    );

    const initialSubscriptionCount = component.subscriptions.length;

    component.loadReleaseLetters();

    expect(component.subscriptions.length).toBe(initialSubscriptionCount + 1);
  });
});
