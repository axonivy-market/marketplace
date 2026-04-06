import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';

import { UndeprecateConfirmDialogComponent } from './undeprecate-confirm-dialog.component';

describe('UndeprecateConfirmDialogComponent', () => {
  let component: UndeprecateConfirmDialogComponent;
  let fixture: ComponentFixture<UndeprecateConfirmDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UndeprecateConfirmDialogComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(UndeprecateConfirmDialogComponent);
    component = fixture.componentInstance;
    component.visible = true;
    component.undeprecateProductId = 'cms-live-editor';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not render dialog when visible is false', () => {
    component.visible = false;
    fixture.detectChanges();

    const wrapper = fixture.debugElement.query(By.css('.custom-modal-wrapper'));
    expect(wrapper).toBeNull();
  });

  it('should render the product id in body', () => {
    const text = fixture.debugElement.query(By.css('.text-muted'));
    expect(text.nativeElement.textContent).toContain('cms-live-editor');
  });

  it('should emit close from header close button and cancel button', () => {
    spyOn(component.closeDialog, 'emit');

    const headerCloseButton = fixture.debugElement.query(By.css('.btn-close'));
    headerCloseButton.triggerEventHandler('click', null);

    const cancelButton = fixture.debugElement.query(By.css('.btn-cancel'));
    cancelButton.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).toHaveBeenCalledTimes(2);
  });

  it('should emit confirm when confirm button is clicked', () => {
    spyOn(component.confirm, 'emit');

    const confirmButton = fixture.debugElement.query(By.css('.btn-danger'));
    confirmButton.triggerEventHandler('click', null);

    expect(component.confirm.emit).toHaveBeenCalled();
  });

  it('should emit close from backdrop click when not undeprecating', () => {
    spyOn(component.closeDialog, 'emit');

    const backdrop = fixture.debugElement.query(By.css('.custom-backdrop'));
    backdrop.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).toHaveBeenCalled();
  });

  it('should not emit close from backdrop click when undeprecating', () => {
    spyOn(component.closeDialog, 'emit');
    component.isUndeprecating = true;
    fixture.detectChanges();

    const backdrop = fixture.debugElement.query(By.css('.custom-backdrop'));
    backdrop.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).not.toHaveBeenCalled();
  });

  it('should disable all action buttons and show spinner when undeprecating', () => {
    component.isUndeprecating = true;
    fixture.detectChanges();

    const headerCloseButton = fixture.debugElement.query(By.css('.btn-close'));
    const cancelButton = fixture.debugElement.query(By.css('.btn-cancel'));
    const confirmButton = fixture.debugElement.query(By.css('.btn-danger'));
    const spinner = fixture.debugElement.query(By.css('.spinner-border'));

    expect(headerCloseButton.properties['disabled']).toBeTrue();
    expect(cancelButton.properties['disabled']).toBeTrue();
    expect(confirmButton.properties['disabled']).toBeTrue();
    expect(spinner).not.toBeNull();
  });
});

