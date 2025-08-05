import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ThemeSelectionComponent } from './theme-selection.component';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { DOCUMENT } from '@angular/common';
import { Theme } from '../../../enums/theme.enum';

import {
  DATA_THEME,
  LIGHT_ICON_CLASS,
  DARK_ICON_CLASS
} from '../../../../shared/constants/common.constant';

describe('ThemeSelectionComponent', () => {
  let component: ThemeSelectionComponent;
  let mockThemeService: jasmine.SpyObj<ThemeService>;
  let fixture: ComponentFixture<ThemeSelectionComponent>;
  let documentRef: Document;

  beforeEach(async () => {
    mockThemeService = jasmine.createSpyObj('ThemeService', [
      'changeTheme',
      'isDarkMode'
    ]);

    await TestBed.configureTestingModule({
      imports: [ThemeSelectionComponent, TranslateModule.forRoot()],
      providers: [
        { provide: ThemeService, useValue: mockThemeService },
        { provide: DOCUMENT, useValue: document }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ThemeSelectionComponent);
    component = fixture.componentInstance;
    documentRef = TestBed.inject(DOCUMENT);
  });

  afterEach(() => {
    documentRef.defaultView?.localStorage.removeItem(DATA_THEME);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle the theme on theme button click', () => {
    const themeButton = fixture.debugElement.query(
      By.css('.header__theme-button')
    );

    themeButton.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(mockThemeService.changeTheme).toHaveBeenCalled();
  });

  it(`should set iconClass to dark if theme is dark`, () => {
    documentRef.defaultView?.localStorage.setItem(DATA_THEME, Theme.DARK);

    component = new ThemeSelectionComponent(documentRef, mockThemeService);

    expect(component.iconClass).toBe(DARK_ICON_CLASS);
  });

  it(`should set iconClass to light if theme is light`, () => {
    documentRef.defaultView?.localStorage.setItem(DATA_THEME, Theme.LIGHT);

    component = new ThemeSelectionComponent(documentRef, mockThemeService);

    expect(component.iconClass).toBe(LIGHT_ICON_CLASS);
  });

  it(`should toggle iconClass from dark to light`, () => {
    documentRef.defaultView?.localStorage.setItem(DATA_THEME, Theme.DARK);

    component = new ThemeSelectionComponent(documentRef, mockThemeService);

    // Simulate light mode after toggle
    documentRef.defaultView?.localStorage.setItem(DATA_THEME, Theme.LIGHT);
    mockThemeService.isDarkMode.and.returnValue(false);

    component.onToggleTheme();

    expect(mockThemeService.changeTheme).toHaveBeenCalled();
    expect(component.iconClass).toBe(LIGHT_ICON_CLASS);
  });

  it(`should toggle iconClass from light to dark`, () => {
    documentRef.defaultView?.localStorage.setItem(DATA_THEME, Theme.LIGHT);

    component = new ThemeSelectionComponent(documentRef, mockThemeService);

    // Simulate dark mode after toggle
    documentRef.defaultView?.localStorage.setItem(DATA_THEME, Theme.DARK);
    mockThemeService.isDarkMode.and.returnValue(true);

    component.onToggleTheme();

    expect(mockThemeService.changeTheme).toHaveBeenCalled();
    expect(component.iconClass).toBe(DARK_ICON_CLASS);
  });
});