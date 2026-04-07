import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BackToTopComponent } from './back-to-top.component';
import { By } from '@angular/platform-browser';
import { describe, beforeEach, it, expect, vi } from 'vitest';

describe('BackToTopComponent', () => {
  let component: BackToTopComponent;
  let fixture: ComponentFixture<BackToTopComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BackToTopComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(BackToTopComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show the button when scroll threshold is reached', () => {
    // Simulate scrolling past the threshold
    window.scrollY = component.backToTopShowThreshold + 1;
    globalThis.dispatchEvent(new Event('scroll'));

    expect(component.showScrollButton).toBe(true);
  });

  it('should hide the button when scrolling above the threshold', () => {
    // Simulate scrolling past the threshold, then scroll back up
    window.scrollY = component.backToTopShowThreshold + 1;
    globalThis.dispatchEvent(new Event('scroll'));
    expect(component.showScrollButton).toBe(true);

    // Scroll up above the threshold
    window.scrollY = component.backToTopShowThreshold - 1;
    globalThis.dispatchEvent(new Event('scroll'));
    expect(component.showScrollButton).toBe(false);
  });

  it('should scroll to top when button is clicked and showScrollButton is true', () => {
    const scrollToSpy = vi.spyOn<Window, 'scrollTo'>(window, 'scrollTo');

    const mockScrollOption = {
      top: 0,
      behavior: 'smooth'
    };
    component.showScrollButton = true;
    component.scrollToTop();

    expect(vi.mocked(scrollToSpy).mock.calls[0]).toEqual([mockScrollOption]);
  });

  it('should not scroll to top when button is clicked and showScrollButton is false', () => {
    const scrollToSpy = vi
      .spyOn<Window, 'scrollTo'>(window, 'scrollTo')
      .mockImplementation(() => {});
    scrollToSpy.mockClear();
    component.showScrollButton = false;
    component.scrollToTop();

    expect(scrollToSpy).not.toHaveBeenCalled();
  });

  it('should render the button when showScrollButton is true', () => {
    component.showScrollButton = true;
    fixture.detectChanges();

    const scrollToTopButtonElement = fixture.debugElement.query(
      By.css('.scroll-to-top')
    );
    expect(scrollToTopButtonElement.nativeElement.classList.contains('show')).toBe(true);
  });

  it('should not render the button when showScrollButton is false', () => {
    component.showScrollButton = false;
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const scrollToTopButtonElement = fixture.debugElement.query(
      By.css('.scroll-to-top')
    );
    expect(scrollToTopButtonElement.nativeElement.classList.contains('show')).toBe(false);
  });
});
