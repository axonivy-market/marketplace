import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BackToTopComponent } from './back-to-top.component';
import { By } from '@angular/platform-browser';

describe('BackToTopComponent', () => {
  let component: BackToTopComponent;
  let fixture: ComponentFixture<BackToTopComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BackToTopComponent],
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
    window.dispatchEvent(new Event('scroll'));

    expect(component.showScrollButton).toBeTrue();
  });

  it('should hide the button when scrolling above the threshold', () => {
    // Simulate scrolling past the threshold, then scroll back up
    window.scrollY = component.backToTopShowThreshold + 1;
    window.dispatchEvent(new Event('scroll'));
    expect(component.showScrollButton).toBeTrue();

    // Scroll up above the threshold
    window.scrollY = component.backToTopShowThreshold - 1;
    window.dispatchEvent(new Event('scroll'));
    expect(component.showScrollButton).toBeFalse();
  });

  it('should scroll to top when button is clicked and showScrollButton is true', () => {
    const scrollToSpy = spyOn<Window>(window, 'scrollTo');
    
    const mockScrollOption = {
      top: 0,
      behavior: "smooth"
  }
    component.showScrollButton = true;
    component.scrollToTop();

    expect(scrollToSpy.calls.argsFor(0)).toEqual([mockScrollOption]);
  });

  it('should not scroll to top when button is clicked and showScrollButton is false', () => {
    const scrollToSpy = spyOn(window, 'scrollTo');
    component.showScrollButton = false;
    component.scrollToTop();

    expect(scrollToSpy).not.toHaveBeenCalled();
  });

  it('should render the button when showScrollButton is true', () => {
    component.showScrollButton = true;
    fixture.detectChanges();

    const buttonElement = fixture.debugElement.query(By.css('.scroll-to-top'));
    expect(getComputedStyle(buttonElement.nativeElement).opacity).toEqual('1');
  });

  it('should not render the button when showScrollButton is false', () => {
    component.showScrollButton = false;
    fixture.detectChanges();

    const buttonElement = fixture.debugElement.query(By.css('.scroll-to-top'));
    expect(getComputedStyle(buttonElement.nativeElement).opacity).toEqual('0');
  });
});
