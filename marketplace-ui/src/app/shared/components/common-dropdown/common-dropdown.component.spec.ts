import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommonDropdownComponent } from './common-dropdown.component';
import { By } from '@angular/platform-browser';
import { TranslateModule, TranslatePipe, TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

describe('CommonDropdownComponent', () => {
  let component: CommonDropdownComponent;
  let fixture: ComponentFixture<CommonDropdownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonDropdownComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(CommonDropdownComponent);
    component = fixture.componentInstance;

    component.items = [{ id: 1, name: 'Item 1' }, { id: 2, name: 'Item 2' }];
    component.selectedItem = component.items[0];
    component.labelKey = 'name';
    component.ariaLabel = 'Dropdown';

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle dropdown on button click', () => {
    const button = fixture.debugElement.query(By.css('button'));
    button.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.isDropdownOpen).toBeTrue();

    button.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.isDropdownOpen).toBeFalse();
  });

  it('should display selected item label', () => {
    const button = fixture.debugElement.query(By.css('button'));
    expect(button.nativeElement.textContent.trim()).toBe('Item 1');
  });

  it('should apply "indicator-arrow__up" class when dropdown is open', () => {
    component.isDropdownOpen = true;
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('button'));
    expect(button.classes['indicator-arrow__up']).toBeTrue();
  });

  it('should call onSelect with the correct item when a dropdown item is clicked', () => {
    spyOn(component, 'onSelect');
    const dropdownItems = fixture.debugElement.queryAll(By.css('.dropdown-item'));

    dropdownItems[1].triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.onSelect).toHaveBeenCalledWith(component.items[1]);
  });

  it('should apply "active" class to the selected item', () => {
    component.selectedItem = component.items[1];
    fixture.detectChanges();

    const dropdownItems = fixture.debugElement.queryAll(By.css('.dropdown-item'));
    expect(dropdownItems[1].classes['active']).toBeTrue();
  });
});
