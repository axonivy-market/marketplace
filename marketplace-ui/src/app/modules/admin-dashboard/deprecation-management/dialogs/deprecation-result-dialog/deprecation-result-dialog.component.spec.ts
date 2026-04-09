import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';

import { DeprecationResultDialogComponent } from './deprecation-result-dialog.component';

describe('DeprecateSuccessDialogComponent', () => {
  let component: DeprecationResultDialogComponent;
  let fixture: ComponentFixture<DeprecationResultDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeprecationResultDialogComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(DeprecationResultDialogComponent);
    component = fixture.componentInstance;
    component.visible = true;
    component.moderatorName = 'moderator';
    fixture.detectChanges();
  });

  it('should not render dialog when visible is false', () => {
    component.visible = false;
    fixture.detectChanges();

    const wrapper = fixture.debugElement.query(By.css('.custom-modal-wrapper'));
    expect(wrapper).toBeNull();
  });

  it('should render undeprecate success text when successMode is undeprecate', () => {
    component.successMode = 'undeprecate';
    fixture.detectChanges();

    const title = fixture.debugElement.query(By.css('.success-title'));
    expect(title.nativeElement.textContent).toContain('common.admin.deprecation.removeDeprecatedSuccess');
  });

  it('should render deprecate success text by default', () => {
    component.successMode = 'deprecate';
    fixture.detectChanges();

    const title = fixture.debugElement.query(By.css('.success-title'));
    expect(title.nativeElement.textContent).toContain('common.admin.deprecation.deprecateSuccess');
  });

  it('should render pull request section only when showPullRequest is true', () => {
    component.showPullRequest = false;
    fixture.detectChanges();

    let prSection = fixture.debugElement.query(By.css('.success-pr-section'));
    expect(prSection).toBeNull();

    component.showPullRequest = true;
    component.pullRequestUrl = 'https://example/pr/1';
    fixture.detectChanges();

    prSection = fixture.debugElement.query(By.css('.success-pr-section'));
    const input = fixture.debugElement.query(By.css('.success-pr-input'));
    expect(prSection).not.toBeNull();
    expect(input.nativeElement.value).toBe('https://example/pr/1');
  });

  it('should emit copy when copy button is clicked', () => {
    vi.spyOn(component.copyPullRequestUrl, 'emit');
    component.showPullRequest = true;
    fixture.detectChanges();

    const copyButton = fixture.debugElement.query(By.css('.copy-btn'));
    copyButton.triggerEventHandler('click', null);

    expect(component.copyPullRequestUrl.emit).toHaveBeenCalled();
  });

  it('should display copied label when isCopySuccessVisible is true', () => {
    component.showPullRequest = true;
    component.isCopySuccessVisible = true;
    fixture.detectChanges();

    const copyButton = fixture.debugElement.query(By.css('.copy-btn'));
    expect(copyButton.nativeElement.textContent).toContain('common.admin.deprecation.copiedLabel');
  });

  it('should emit close from close button and backdrop click when not closing', () => {
    vi.spyOn(component.closeDialog, 'emit');

    const closeButton = fixture.debugElement.query(By.css('.success-close-btn'));
    closeButton.triggerEventHandler('click', null);

    const backdrop = fixture.debugElement.query(By.css('.custom-backdrop'));
    backdrop.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).toHaveBeenCalledTimes(2);
  });

  it('should not emit close from backdrop click when closing', () => {
    vi.spyOn(component.closeDialog, 'emit');
    component.isClosing = true;
    fixture.detectChanges();

    const backdrop = fixture.debugElement.query(By.css('.custom-backdrop'));
    backdrop.triggerEventHandler('click', null);

    expect(component.closeDialog.emit).not.toHaveBeenCalled();
  });

  it('should disable close button when closing', () => {
    component.isClosing = true;
    fixture.detectChanges();

    const closeButton = fixture.debugElement.query(By.css('.success-close-btn'));
    expect(closeButton.properties['disabled']).toBe(true);
  });
});

