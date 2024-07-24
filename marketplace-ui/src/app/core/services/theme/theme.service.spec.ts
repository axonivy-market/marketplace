import { TestBed } from '@angular/core/testing';
import { Theme } from '../../../shared/enums/theme.enum';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let service: ThemeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [ThemeService],
    });
    service = TestBed.inject(ThemeService);
  });

  it('should be created', () => {
    document.defaultView?.localStorage.clear();
    expect(service).toBeTruthy();
  });

  it('setTheme light', () => {
    service.setTheme(Theme.LIGHT);
    expect(service.theme()).toEqual(Theme.LIGHT);
  });

  it('setTheme dark', () => {
    service.setTheme(Theme.DARK);
    expect(service.theme()).toEqual(Theme.DARK);
  });

  it('changeTheme to light', () => {
    service.setTheme(Theme.DARK);
    service.changeTheme();
    expect(service.theme()).toEqual(Theme.LIGHT);
  });

  it('changeTheme to dark', () => {
    service.setTheme(Theme.LIGHT);
    service.changeTheme();
    expect(service.theme()).toEqual(Theme.DARK);
  });
});
