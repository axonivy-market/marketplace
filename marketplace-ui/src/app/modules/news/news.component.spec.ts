import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewsComponent } from './news.component';
import { ElementRef, PLATFORM_ID } from '@angular/core';
import { AdminDashboardService } from '../admin-dashboard/admin-dashboard.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { PageTitleService } from '../../shared/services/page-title.service';
import { MarkdownService } from '../../shared/services/markdown.service';
import { LoadingService } from '../../core/services/loading/loading.service';
import { of } from 'rxjs';

describe('NewsComponent', () => {
  let component: NewsComponent;
  let fixture: ComponentFixture<NewsComponent>;

  let mockAdminDashboardService: any;
  let mockPageTitleService: any;
  let mockMarkdownService: any;

  beforeEach(async () => {
    mockAdminDashboardService = {
      getReleaseLetters: jasmine.createSpy().and.returnValue(
        of({
          _embedded: { releaseLetterModelList: [] },
          _links: {},
          page: { number: 0, totalPages: 0 }
        })
      )
    };

    mockPageTitleService = {
      setTitleOnLangChange: jasmine.createSpy()
    };

    mockMarkdownService = {
      parseMarkdown: jasmine
        .createSpy()
        .and.callFake((c: string) => `<p>${c}</p>`)
    };

    // mockLoadingService = {
    //   isLoading: jasmine.createSpy().and.returnValue(false)
    // };

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
    expect(result.latest).toBeFalse();
    expect(result.createdAt).toBe('2026-02-02');
    expect(mockMarkdownService.parseMarkdown).toHaveBeenCalledWith('markdown');
  });

  it('should render markdown and sanitize html', () => {
    const result = component.renderReleaseLetterContent('abc');

    expect(mockMarkdownService.parseMarkdown).toHaveBeenCalledWith('abc');
    expect(result).toBeTruthy();
  });

  it('should return translated sprint title', () => {
    spyOn(component.translateService, 'instant').and.returnValue('Sprint ');

    const result = component.getSprintTitle('S100');

    expect(component.translateService.instant).toHaveBeenCalledWith(
      'common.admin.news.sprint'
    );
    expect(result).toBe('Sprint S100');
  });

  it('should return false if no links or pages', () => {
    component.newsLinks = undefined as any;
    component.newsPages = undefined as any;

    expect(component.hasMoreReleaseLetters()).toBeFalse();
  });

  it('should return true if next page exists', () => {
    component.newsPages = { number: 0, totalPages: 2 } as any;
    component.newsLinks = {
      next: { href: 'next-page-url' }
    } as any;

    expect(component.hasMoreReleaseLetters()).toBeTrue();
  });

  it('should return false if already at last page', () => {
    component.newsPages = { number: 2, totalPages: 2 } as any;
    component.newsLinks = {} as any;

    expect(component.hasMoreReleaseLetters()).toBeFalse();
  });

  it('should set empty title when no release letters returned', () => {
    spyOn(component.translateService, 'instant').and.returnValue('No releases');

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

    mockAdminDashboardService.getReleaseLetters.and.returnValue(of(response));

    component.loadReleaseLetters();

    const list = component.releaseLetterSafeHtmlContentList();

    expect(list.length).toBe(1);
    expect(component.newsLinks).toEqual(response._links);
    expect(component.newsPages).toEqual(response.page);
    expect(component.releaseLetterCriteria.nextPageHref).toBe('next-url');
  });

  it('should setup intersection observer and call loadReleaseLetters when intersecting', () => {
    const observeSpy = jasmine.createSpy('observe');

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

    spyOn(component, 'hasMoreReleaseLetters').and.returnValue(true);
    spyOn(component.loadingService, 'isLoading').and.returnValue(false);
    spyOn(component, 'loadReleaseLetters');

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

  it('should unsubscribe all subscriptions on destroy', () => {
    const unsubscribeSpy = jasmine.createSpy('unsubscribe');

    component.subscriptions = [{ unsubscribe: unsubscribeSpy } as any];

    component.ngOnDestroy();

    expect(unsubscribeSpy).toHaveBeenCalled();
  });
});
