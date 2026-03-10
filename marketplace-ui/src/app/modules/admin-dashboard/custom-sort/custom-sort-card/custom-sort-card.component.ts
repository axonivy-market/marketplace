import { Component, Input, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-custom-sort-card',
  imports: [],
  templateUrl: './custom-sort-card.component.html',
  styleUrl: './custom-sort-card.component.scss',
  encapsulation: ViewEncapsulation.Emulated
})
export class CustomSortCardComponent {
  @Input() title = '';
  @Input() badge: string | number = '';
}
