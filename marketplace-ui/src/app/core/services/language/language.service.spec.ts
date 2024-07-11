import { TestBed } from '@angular/core/testing';
import { LanguageService } from './language.service';
import { Language } from '../../../shared/enums/language.enum';

describe('LanguageService', () => {
  let service: LanguageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [LanguageService],
    });
    service = TestBed.inject(LanguageService);
  });

  it('should be created', () => {
    document.defaultView?.localStorage.clear();
    expect(service).toBeTruthy();
  });

  it('should get default language en', () => {
    document.defaultView?.localStorage.clear();
    expect(service.getSelectedLanguage()).toEqual(Language.EN);
  });

  it('should change to language de-DE', ()=> {
    service.loadLanguage("de");
    expect(service.getSelectedLanguage()).toEqual(Language.DE);
  });
});
