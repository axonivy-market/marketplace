import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgClass, NgForOf } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

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
export class CommonDropdownComponent {
  @Input() items: any[] = [];
  @Input() selectedItem: any;
  @Input() labelKey: string = '';
  @Input() dropdownClass: string = '';
  @Input() buttonClass: string = '';
  @Input() ariaLabel: string = '';

  @Output() itemSelected = new EventEmitter<any>();

  isDropdownOpen = false;

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  onSelect(item: any) {
    this.itemSelected.emit(item);
    this.isDropdownOpen = false;
  }
}
