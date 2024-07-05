import { Component, inject } from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';

@Component({
  selector: 'app-theme-selection',
  standalone: true,
  imports: [],
  templateUrl: './theme-selection.component.html',
  styleUrl: './theme-selection.component.scss'
})
export class ThemeSelectionComponent {
  themeService = inject(ThemeService);
}
