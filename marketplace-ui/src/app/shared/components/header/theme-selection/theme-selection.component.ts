import { Component, inject } from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-theme-selection',
  standalone: true,
  imports: [NgIf],
  templateUrl: './theme-selection.component.html',
  styleUrl: './theme-selection.component.scss'
})
export class ThemeSelectionComponent {
  themeService = inject(ThemeService);
}
