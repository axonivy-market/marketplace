import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { LanguageSelectionComponent } from './language-selection.component';
import { Language } from '../../../enums/language.enum';

describe('LanguageSelectionComponent', () => {
  let component: LanguageSelectionComponent;
  let fixture: ComponentFixture<LanguageSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LanguageSelectionComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(LanguageSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('selectLanguage should call translateService', () => {
    spyOn(component.translateService, 'use').and.stub();
    component.onSelectLanguage(Language.EN);
    expect(component.translateService.use).toHaveBeenCalled();
  });
});
