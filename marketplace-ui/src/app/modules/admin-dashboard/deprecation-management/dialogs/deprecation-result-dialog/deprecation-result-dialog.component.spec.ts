import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';

import { DeprecationResultDialogComponent } from './deprecation-result-dialog.component';
import { DeprecationMode } from '../../../../../shared/enums/deprecation-mode.enum';

describe('DeprecateSuccessDialogComponent', () => {
  let component: DeprecationResultDialogComponent;
  let fixture: ComponentFixture<DeprecationResultDialogComponent>;

  afterEach(() => {
    const globalWrapper = document.querySelectorAll('.custom-modal-wrapper');
    globalWrapper.forEach(n => n.remove());
    const globalBackdrop = document.querySelectorAll('.custom-backdrop');
    globalBackdrop.forEach(n => n.remove());
  });

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
    expect(component.visible).toBe(false);
  });

  it('should render undeprecate success text when successMode is undeprecate', () => {
    const localFixture = TestBed.createComponent(DeprecationResultDialogComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.visible = true;
    localComponent.successMode = DeprecationMode.UNDEPRECATE;
    localFixture.detectChanges();

    const titleEl = document.querySelector('.success-title') as HTMLElement | null;
    expect(titleEl?.textContent).toContain('common.admin.deprecation.removeDeprecatedSuccess');
  });

  it('should render deprecate success text by default', () => {
    component.successMode = DeprecationMode.DEPRECATE;
    fixture.detectChanges();

    const title = fixture.debugElement.query(By.css('.success-title'));
    expect(title.nativeElement.textContent).toContain('common.admin.deprecation.deprecateSuccess');
  });

  it('should render pull request section only when showPullRequest is true', () => {
    const noPrFixture = TestBed.createComponent(DeprecationResultDialogComponent);
    const noPrComp = noPrFixture.componentInstance;
    noPrComp.visible = true;
    noPrComp.showPullRequest = false;
    noPrFixture.detectChanges();
    let prSection = document.querySelector('.success-pr-section');
    expect(prSection).toBeNull();

    const prFixture = TestBed.createComponent(DeprecationResultDialogComponent);
    const prComp = prFixture.componentInstance;
    prComp.visible = true;
    prComp.showPullRequest = true;
    prComp.pullRequestUrl = 'https://example/pr/1';
    prFixture.detectChanges();

    prSection = document.querySelector('.success-pr-section');
    const input = document.querySelector('.success-pr-input') as HTMLInputElement | null;
    expect(prSection).not.toBeNull();
    expect(input?.value).toBe('https://example/pr/1');
  });

  it('should emit copy when copy button is clicked', () => {
    const localFixture = TestBed.createComponent(DeprecationResultDialogComponent);
    const localComponent = localFixture.componentInstance;
    vi.spyOn(localComponent.copyPullRequestUrl, 'emit');
    localComponent.visible = true;
    localComponent.showPullRequest = true;
    localFixture.detectChanges();

    const copyButton = document.querySelector('.copy-btn') as HTMLElement | null;
    copyButton?.click();

    expect(localComponent.copyPullRequestUrl.emit).toHaveBeenCalled();
  });

  it('should display copied label when isCopySuccessVisible is true', () => {
    const localFixture = TestBed.createComponent(DeprecationResultDialogComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.visible = true;
    localComponent.showPullRequest = true;
    localComponent.isCopySuccessVisible = true;
    localFixture.detectChanges();

    const copyButton = document.querySelector('.copy-btn') as HTMLElement | null;
    expect(copyButton?.textContent).toContain('common.admin.deprecation.copiedLabel');
  });

  it('should emit close from close button and backdrop click when not closing', () => {
    vi.spyOn(component.closeDialog, 'emit');

    const closeButton = fixture.debugElement.query(By.css('.btn-close'));
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
    const localFixture = TestBed.createComponent(DeprecationResultDialogComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.visible = true;
    localComponent.isClosing = true;
    localFixture.detectChanges();

    const closeButton = document.querySelector('.btn-close') as HTMLButtonElement | null;
    expect(closeButton?.disabled).toBe(true);
  });
});

