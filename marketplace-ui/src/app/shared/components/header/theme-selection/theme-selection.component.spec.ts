import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ThemeSelectionComponent } from './theme-selection.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

describe('ThemeSelectionComponent', () => {
  let component: ThemeSelectionComponent;
  let fixture: ComponentFixture<ThemeSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ThemeSelectionComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(ThemeSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle the theme on theme button click', () => {
    spyOn(component.themeService, 'changeTheme').and.callThrough();
    const themeButton = fixture.debugElement.query(
      By.css('.header__theme-button')
    );

    // Click the theme button
    themeButton.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.themeService.changeTheme).toHaveBeenCalled();
  });
});
