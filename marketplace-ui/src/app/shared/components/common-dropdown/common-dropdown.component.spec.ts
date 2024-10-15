import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommonDropdownComponent } from './common-dropdown.component';
import { By } from '@angular/platform-browser';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { ItemDropdown } from '../../models/item-dropdown.model';
import { ElementRef } from '@angular/core';

declare const viewport: Viewport;
describe('CommonDropdownComponent', () => {
  let component: CommonDropdownComponent<string>;
  let fixture: ComponentFixture<CommonDropdownComponent<string>>;
  let elementRef: ElementRef;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonDropdownComponent, TranslateModule.forRoot()],
      providers: [TranslateService],
    }).compileComponents();

    fixture = TestBed.createComponent(CommonDropdownComponent);
    component = fixture.componentInstance;
    elementRef = fixture.debugElement.injector.get(ElementRef);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle the dropdown menu when button is clicked', () => {
    const button = fixture.debugElement.query(By.css('button'));
    button.triggerEventHandler('click', null);
    fixture.detectChanges();
    expect(component.isDropdownOpen).toBeTrue();

    button.triggerEventHandler('click', null);
    fixture.detectChanges();
    expect(component.isDropdownOpen).toBeFalse();
  });

  it('should close the dropdown when clicking outside', () => {
    component.isDropdownOpen = true;
    fixture.detectChanges();

    const event = new MouseEvent('click');
    document.dispatchEvent(event);
    fixture.detectChanges();

    expect(component.isDropdownOpen).toBeFalse();
  });


  it('should apply "indicator-arrow__up" class when dropdown is open', () => {
    component.isDropdownOpen = true;
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('button'));
    expect(button.classes['indicator-arrow__up']).toBeTrue();
  });

  it('should emit selected item and close the dropdown when an item is clicked', () => {
    spyOn(component.itemSelected, 'emit');

    const items: ItemDropdown<string>[] = [
      { label: 'Item 1', value: 'item1' },
      { label: 'Item 2', value: 'item2' },
    ];
    component.items = items;
    component.isDropdownOpen = true;
    fixture.detectChanges();

    const dropdownItems = fixture.debugElement.queryAll(By.css('.dropdown-item'));
    dropdownItems[0].triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.itemSelected.emit).toHaveBeenCalledWith(items[0]);
    expect(component.isDropdownOpen).toBeFalse();
  });

  it('should call the translate service to get the translated label', () => {
    const translateService = TestBed.inject(TranslateService);
    spyOn(translateService, 'instant').and.returnValue('Translated Label');

    const item = { label: 'originalLabel', value: 'item1' };
    const result = component.isActiveItem(item, 'Translated Label');

    expect(result).toBeTrue();
    expect(translateService.instant).toHaveBeenCalledWith('originalLabel');
  });

  it('should apply scrollbar when dropdown is opened and items overflow', () => {
    viewport.set(1920);

    component.isDropdownOpen = true;
    fixture.detectChanges();

    const dropdownMenu = fixture.debugElement.query(By.css('.dropdown-menu')).nativeElement;

    const maxHeight = getComputedStyle(dropdownMenu).maxHeight;
    const overflow = getComputedStyle(dropdownMenu).overflow;

    expect(maxHeight).toBe('440px');
    expect(overflow).toBe('auto');
  });

  it('should apply scrollbar when dropdown is opened in smaller screen', () => {
    viewport.set(430);

    component.isDropdownOpen = true;
    fixture.detectChanges();

    const dropdownMenu = fixture.debugElement.query(By.css('.dropdown-menu')).nativeElement;

    const maxHeight = getComputedStyle(dropdownMenu).maxHeight;
    const overflow = getComputedStyle(dropdownMenu).overflow;

    expect(maxHeight).toBe('220px');
    expect(overflow).toBe('auto');
  });
});
