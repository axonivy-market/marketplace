import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { CustomSortCardComponent } from './custom-sort-card.component';

describe('CustomSortCardComponent', () => {
  let fixture: ComponentFixture<CustomSortCardComponent>;
  let component: CustomSortCardComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomSortCardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(CustomSortCardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render bound title and badge inputs', () => {
    component.title = 'Sorted Extensions';
    component.badge = 5;
    fixture.detectChanges();

    const h2 = fixture.debugElement.query(By.css('h2')).nativeElement;
    const badge = fixture.debugElement.query(By.css('.badge')).nativeElement;

    expect(h2.textContent.trim()).toBe('Sorted Extensions');
    expect(badge.textContent.trim()).toBe('5');
  });

  it('should show structural elements expected by consumers', () => {
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('.table-card')))
      .withContext('card wrapper')
      .not.toBeNull();
    expect(fixture.debugElement.query(By.css('header')))
      .withContext('header region')
      .not.toBeNull();
    expect(fixture.debugElement.query(By.css('.table-scroll')))
      .withContext('scroll container')
      .not.toBeNull();
    expect(fixture.debugElement.query(By.css('table')))
      .withContext('inner table')
      .not.toBeNull();
  });

  it('should keep header badge present even when inputs change', () => {
    component.title = 'Sorted Extensions';
    component.badge = '';
    fixture.detectChanges();

    component.title = 'Available Extensions';
    component.badge = '99/99';
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('.badge')).nativeElement;
    expect(badge.textContent.trim()).toBe('99/99');
  });
});
