import { Component } from '@angular/core';
import { NgForOf } from '@angular/common';

@Component({
  selector: 'app-common-dropdown',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './common-dropdown.component.html',
  styleUrl: './common-dropdown.component.scss'
})
export class CommonDropdownComponent {
  isDropdownOpen = false;
  selectedOption: string | null = null;
  options = ['Option 1', 'Option 2', 'Option 3', 'Option 4'];

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  selectOption(option: string) {
    this.selectedOption = option;
    this.isDropdownOpen = false;
  }
}
