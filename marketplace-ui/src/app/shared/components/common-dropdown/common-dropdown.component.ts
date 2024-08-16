import { Component, ElementRef, EventEmitter, HostListener, inject, Input, Output } from '@angular/core';
import { NgClass, NgForOf } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ItemDropdown } from '../../models/item-dropdown.model';

@Component({
  selector: 'app-common-dropdown',
  standalone: true,
  imports: [
    NgForOf,
    NgClass,
    TranslateModule
  ],
  templateUrl: './common-dropdown.component.html',
  styleUrl: './common-dropdown.component.scss'
})
export class CommonDropdownComponent <T extends string> {
  translateService = inject(TranslateService);
  @Input() items:  ItemDropdown<T>[] = [];
  @Input() selectedItem: T | undefined;
  @Input() labelKey: string = '';
  @Input() buttonClass: string = '';
  @Input() ariaLabel: string = '';

  @Output() itemSelected = new EventEmitter<ItemDropdown<T>>();
  elementRef = inject(ElementRef);
  isDropdownOpen = false;
  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  onSelect(item: ItemDropdown<T>) {
    this.itemSelected.emit(item);
    this.isDropdownOpen = false;
  }

  isActiveItem(value: ItemDropdown, selectedItem: T | undefined): boolean {
    return this.translateService.instant(value.label) === selectedItem;
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    if (!this.elementRef.nativeElement.querySelector('.dropdown').contains(event.target) && this.isDropdownOpen) {
      this.isDropdownOpen = !this.isDropdownOpen;
    }
  }
}
