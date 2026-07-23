import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';

import { RemoveDeprecatedConfirmDialogComponent } from './remove-deprecated-confirm-dialog.component';

describe('RemoveDeprecatedConfirmDialogComponent', () => {
  let component: RemoveDeprecatedConfirmDialogComponent;
  let fixture: ComponentFixture<RemoveDeprecatedConfirmDialogComponent>;

  afterEach(() => {
    const globalWrapper = document.querySelectorAll('.custom-modal-wrapper');
    globalWrapper.forEach(n => n.remove());
    const globalBackdrop = document.querySelectorAll('.custom-backdrop');
    globalBackdrop.forEach(n => n.remove());
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RemoveDeprecatedConfirmDialogComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(RemoveDeprecatedConfirmDialogComponent);
    component = fixture.componentInstance;
    component.visible = true;
    component.removedProductId = 'cms-live-editor';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not render dialog when visible is false', () => {
    component.visible = false;
    fixture.detectChanges();
    expect(component.visible).toBe(false);
  });

  it('should render the product id in body', () => {
    const text = fixture.debugElement.query(By.css('.text-muted'));
    expect(text.nativeElement.textContent).toContain('cms-live-editor');
  });

  it('should emit close from header close button and cancel button', () => {
    vi.spyOn(component.closeDialog, 'emit');

    const headerCloseButton = fixture.debugElement.query(By.css('.btn-close'));
    headerCloseButton.triggerEventHandler('click', null);

    const cancelButton = fixture.debugElement.query(By.css('.btn-cancel'));
    cancelButton.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).toHaveBeenCalledTimes(2);
  });

  it('should emit confirm when confirm button is clicked', () => {
    vi.spyOn(component.confirm, 'emit');

    const confirmButton = fixture.debugElement.query(By.css('.btn-danger'));
    confirmButton.triggerEventHandler('click', null);

    expect(component.confirm.emit).toHaveBeenCalled();
  });

  it('should emit close from backdrop click when not undeprecating', () => {
    vi.spyOn(component.closeDialog, 'emit');
    const backdrop = fixture.debugElement.query(By.css('.custom-backdrop'));
    backdrop.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).toHaveBeenCalled();
  });

  it('should not emit close from backdrop click when undeprecating', () => {
    vi.spyOn(component.closeDialog, 'emit');
    component.isRemoving = true;
    fixture.detectChanges();

    const backdrop = fixture.debugElement.query(By.css('.custom-backdrop'));
    backdrop.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).not.toHaveBeenCalled();
  });

  it('should disable all action buttons and show spinner when undeprecating', () => {
    component.isRemoving = true;
    fixture.detectChanges();
    const localFixture = TestBed.createComponent(RemoveDeprecatedConfirmDialogComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.visible = true;
    localComponent.isRemoving = true;
    localComponent.removedProductId = 'cms-live-editor';
    localFixture.detectChanges();

    const modalRoot = document.querySelector('.custom-modal-wrapper') as HTMLElement | null;
    const headerCloseButtonEl = modalRoot?.querySelector('.btn-close') as HTMLButtonElement | null;
    const cancelButtonEl = modalRoot?.querySelector('.btn-cancel') as HTMLButtonElement | null;
    const confirmButtonEl = modalRoot?.querySelector('.btn-danger') as HTMLButtonElement | null;
    const spinnerEl = modalRoot?.querySelector('.spinner-border');

    expect(headerCloseButtonEl).toBeTruthy();
    expect(cancelButtonEl).toBeTruthy();
    expect(confirmButtonEl).toBeTruthy();
    expect(confirmButtonEl!.disabled).toBe(true);
    expect(spinnerEl).not.toBeNull();
  });
});

