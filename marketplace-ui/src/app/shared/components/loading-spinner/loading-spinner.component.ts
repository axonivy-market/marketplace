import { Component } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [
    NgClass
  ],
  templateUrl: './loading-spinner.component.html',
  styleUrl: './loading-spinner.component.scss'
})
export class LoadingSpinnerComponent {
  spinners = Array(5).fill(null);
}
