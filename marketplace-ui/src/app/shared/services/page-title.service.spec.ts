import { afterEach, beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { TranslateService, LangChangeEvent } from '@ngx-translate/core';
import { Subject, of } from 'rxjs';
import { PageTitleService } from './page-title.service';

describe('PageTitleService', () => {
  let service: PageTitleService;
  let translateService: MockedObject<TranslateService>;
  let titleService: MockedObject<Title>;
  let langChangeSubject: Subject<LangChangeEvent>;

  beforeEach(() => {
    langChangeSubject = new Subject<LangChangeEvent>();
    const translateSpy = {
      get: vi.fn().mockName('TranslateService.get'),
      onLangChange: langChangeSubject.asObservable()
    };
    const titleSpy = {
      setTitle: vi.fn().mockName('Title.setTitle')
    };

    TestBed.configureTestingModule({
      providers: [
        PageTitleService,
        { provide: TranslateService, useValue: translateSpy },
        { provide: Title, useValue: titleSpy }
      ]
    });

    service = TestBed.inject(PageTitleService);
    translateService = TestBed.inject(
      TranslateService
    ) as MockedObject<TranslateService>;
    titleService = TestBed.inject(Title) as MockedObject<Title>;
  });

  afterEach(() => {
    service.ngOnDestroy();
    langChangeSubject.complete();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should set initial title with translated text', () => {
    const titleLabel = 'common.preview.pageTitle';
    const pageTitle = 'Release Preview';
    translateService.get.mockReturnValue(of(pageTitle));

    service.setTitleOnLangChange(titleLabel);

    expect(translateService.get).toHaveBeenCalledWith(titleLabel);
    expect(titleService.setTitle).toHaveBeenCalledWith(pageTitle);
  });

  it('should update title when language changes', () => {
    const titleLabel = 'common.preview.pageTitle';
    const enTitle = 'Release Preview';
    const deTitle = 'Veröffentlichungsvorschau';

    translateService.get
      .mockReturnValueOnce(of(enTitle))
      .mockReturnValueOnce(of(deTitle));

    service.setTitleOnLangChange(titleLabel);

    const langChangeEvent: LangChangeEvent = {
      lang: 'de',
      translations: {}
    };
    langChangeSubject.next(langChangeEvent);

    expect(translateService.get).toHaveBeenCalledTimes(2);
    expect(translateService.get).toHaveBeenCalledWith(titleLabel);
    expect(titleService.setTitle).toHaveBeenCalledTimes(2);
    expect(titleService.setTitle).toHaveBeenCalledWith(enTitle);
    expect(titleService.setTitle).toHaveBeenCalledWith(deTitle);
  });

  it('should unsubscribe from language change subscription', () => {
    const titleLabel = 'common.preview.pageTitle';
    translateService.get.mockReturnValue(of('Release Preview'));

    service.setTitleOnLangChange(titleLabel);

    const subscription = (service as any).langSub;
    vi.spyOn(subscription, 'unsubscribe');

    service.ngOnDestroy();

    expect(subscription.unsubscribe).toHaveBeenCalled();
  });

  it('should handle ngOnDestroy when no subscription exists', () => {
    expect(() => service.ngOnDestroy()).not.toThrow();
  });
});
