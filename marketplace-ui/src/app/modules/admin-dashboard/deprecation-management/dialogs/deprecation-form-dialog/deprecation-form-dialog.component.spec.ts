import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';

import { DeprecationFormDialogComponent } from './deprecation-form-dialog.component';
import { PullRequestAction } from '../../../../../shared/enums/pullrequest-action';

describe('DeprecateFormDialogComponent', () => {
  let component: DeprecationFormDialogComponent;
  let fixture: ComponentFixture<DeprecationFormDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeprecationFormDialogComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(DeprecationFormDialogComponent);
    component = fixture.componentInstance;
    component.visible = true;
    component.deprecationRequest = {
      hasAlternativeExtension: false,
      alternativeExtension: '',
      successorUrl: '',
      isAddReadme: false,
      isDeprecated: false,
      pullRequestAction: PullRequestAction.ADD,
      deprecationRequester: 'tester'
    };
    fixture.detectChanges();
  });

  it('should not render dialog when visible is false', () => {
    component.visible = false;
    fixture.detectChanges();

    const wrapper = fixture.debugElement.query(By.css('.custom-modal-wrapper'));
    expect(wrapper).toBeNull();
  });

  it('should render product dropdown and emit selected product', () => {
    vi.spyOn(component.selectProduct, 'emit');
    component.dropdownOpen = true;
    component.filteredProductIds = ['cms-live-editor', 'rtf-factory'];
    fixture.detectChanges();

    const items = fixture.debugElement.queryAll(By.css('.dropdown-item'));
    expect(items.length).toBe(2);

    items[0].triggerEventHandler('mousedown', null);
    expect(component.selectProduct.emit).toHaveBeenCalledWith('cms-live-editor');
  });

  it('should conditionally render replacement fields based on checkbox state', () => {
    expect(fixture.debugElement.query(By.css('#has-product-replacement-checkbox'))).not.toBeNull();
    expect(fixture.debugElement.query(By.css('input[placeholder*="alternativeExtensionInputPlaceholder"]'))).toBeNull();
    expect(fixture.debugElement.query(By.css('input[placeholder*="successorInputPlaceholder"]'))).toBeNull();

    component.deprecationRequest.hasAlternativeExtension = true;
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('input[placeholder*="alternativeExtensionInputPlaceholder"]'))).not.toBeNull();
    expect(fixture.debugElement.query(By.css('input[placeholder*="successorInputPlaceholder"]'))).not.toBeNull();
  });

  it('should show validation error for productId when present', () => {
    component.validationErrors = { productId: 'Extension ID is required' };
    fixture.detectChanges();

    const error = fixture.debugElement.query(By.css('.invalid-feedback'));
    expect(error.nativeElement.textContent).toContain('Extension ID is required');
  });

  it('should emit close from header close button and backdrop click when not deprecating', () => {
    vi.spyOn(component.closeDialog, 'emit');

    const headerCloseButton = fixture.debugElement.query(By.css('.btn-close'));
    headerCloseButton.triggerEventHandler('click', null);

    const backdrop = fixture.debugElement.query(By.css('.custom-backdrop'));
    backdrop.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).toHaveBeenCalledTimes(2);
  });

  it('should not emit close from backdrop click when deprecating', () => {
    vi.spyOn(component.closeDialog, 'emit');
    component.isDeprecating = true;
    fixture.detectChanges();

    const backdrop = fixture.debugElement.query(By.css('.custom-backdrop'));
    backdrop.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).not.toHaveBeenCalled();
  });

  it('should emit readmeChecked and submit', () => {
    vi.spyOn(component.readmeChecked, 'emit');
    vi.spyOn(component.submitForm, 'emit');

    const readmeCheckbox = fixture.debugElement.query(By.css('#add-readme-checkbox'));
    readmeCheckbox.triggerEventHandler('click', null);

    const submitButton = fixture.debugElement.query(By.css('.btn-primary'));
    submitButton.triggerEventHandler('click', null);

    expect(component.readmeChecked.emit).toHaveBeenCalled();
    expect(component.submitForm.emit).toHaveBeenCalled();
  });

  it('should disable actions and show spinner when deprecating', () => {
    component.isDeprecating = true;
    fixture.detectChanges();

    const closeButton = fixture.debugElement.query(By.css('.btn-close'));
    const submitButton = fixture.debugElement.query(By.css('.btn-primary'));
    const spinner = fixture.debugElement.query(By.css('.spinner-border'));

    expect(closeButton.properties['disabled']).toBe(true);
    expect(submitButton.properties['disabled']).toBe(true);
    expect(spinner).not.toBeNull();
  });
});

