import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgbRating } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [NgbRating],
  templateUrl: './star-rating.component.html',
  styleUrl: './star-rating.component.scss'
})
export class StarRatingComponent {
  @Input() rate = 0;
  @Input() isReadOnly = false;
  @Input() starClass = '';
  @Input() ratingStarsClass = '';

  @Output() rateChange = new EventEmitter<number>();

  onRateChange(newRate: number): void {
    this.rate = newRate;
    this.rateChange.emit(newRate);
  }
}
