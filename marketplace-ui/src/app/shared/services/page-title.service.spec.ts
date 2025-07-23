import { TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { TranslateService, LangChangeEvent } from '@ngx-translate/core';
import { Subject, of } from 'rxjs';
import { PageTitleService } from './page-title.service';

describe('PageTitleService', () => {
  let service: PageTitleService;
  let translateService: jasmine.SpyObj<TranslateService>;
  let titleService: jasmine.SpyObj<Title>;
  let langChangeSubject: Subject<LangChangeEvent>;

  beforeEach(() => {
    langChangeSubject = new Subject<LangChangeEvent>();
    const translateSpy = jasmine.createSpyObj('TranslateService', ['get'], {
      onLangChange: langChangeSubject.asObservable()
    });
    const titleSpy = jasmine.createSpyObj('Title', ['setTitle']);

    TestBed.configureTestingModule({
      providers: [
        PageTitleService,
        { provide: TranslateService, useValue: translateSpy },
        { provide: Title, useValue: titleSpy }
      ]
    });

    service = TestBed.inject(PageTitleService);
    translateService = TestBed.inject(TranslateService) as jasmine.SpyObj<TranslateService>;
    titleService = TestBed.inject(Title) as jasmine.SpyObj<Title>;
  });

  afterEach(() => {
    service.ngOnDestroy();
    langChangeSubject.complete();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should subscribe to language changes', () => {
    const titleLabel = 'common.preview.pageTitle';

    service.setTitleOnLangChange(titleLabel);

    // Service should subscribe but not set title initially
    expect(translateService.get).not.toHaveBeenCalled();
    expect(titleService.setTitle).not.toHaveBeenCalled();
    expect((service as any).langSub).toBeDefined();
  });

  it('should update title when language changes', () => {
    const titleLabel = 'common.preview.pageTitle';
    const translatedTitle = 'Veröffentlichungsvorschau';

    translateService.get.and.returnValue(of(translatedTitle));
    service.setTitleOnLangChange(titleLabel);

    const langChangeEvent: LangChangeEvent = {
      lang: 'de',
      translations: {}
    };
    langChangeSubject.next(langChangeEvent);

    expect(translateService.get).toHaveBeenCalledWith(titleLabel);
    expect(titleService.setTitle).toHaveBeenCalledWith(translatedTitle);
  });

  it('should handle multiple language changes', () => {
    const titleLabel = 'common.preview.pageTitle';
    const deTitle = 'Veröffentlichungsvorschau';
    const enTitle = 'Release Preview';

    translateService.get.and.returnValues(
      of(deTitle),
      of(enTitle)
    );

    service.setTitleOnLangChange(titleLabel);

    langChangeSubject.next({ lang: 'de', translations: {} });

    langChangeSubject.next({ lang: 'en', translations: {} });

    expect(translateService.get).toHaveBeenCalledTimes(2);
    expect(translateService.get).toHaveBeenCalledWith(titleLabel);

  });

  it('should handle ngOnDestroy when no subscription exists', () => {
    expect(() => service.ngOnDestroy()).not.toThrow();
  });
});
