import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { LanguageSelectionComponent } from './language-selection.component';
import { Language } from '../../../enums/language.enum';
import { describe, beforeEach, it, expect, vi } from 'vitest';

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
    vi.spyOn(component.translateService, 'use').mockImplementation(() => {});
    component.onSelectLanguage(Language.EN);
    expect(component.translateService.use).toHaveBeenCalled();
  });
});
