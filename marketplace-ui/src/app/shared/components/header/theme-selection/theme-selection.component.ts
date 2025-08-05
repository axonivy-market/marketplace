import { Component, Inject } from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { NgClass, NgIf, DOCUMENT } from '@angular/common';

const THEME_ICON_CLASS = 'theme-icon-class';
const LIGHT_ICON_CLASS = 'bi-moon';
const DARK_ICON_CLASS = 'bi-sun';

@Component({
  selector: 'app-theme-selection',
  standalone: true,
  imports: [NgClass, NgIf],
  templateUrl: './theme-selection.component.html',
  styleUrl: './theme-selection.component.scss'
})
export class ThemeSelectionComponent {
  iconClass: string | null = null;

  constructor(
    @Inject(DOCUMENT) private readonly document: Document,
    public themeService: ThemeService
  ) {
    this.iconClass =
      this.document.documentElement.getAttribute(THEME_ICON_CLASS);
  }

  onToggleTheme(): void {
    this.themeService.changeTheme();
    this.iconClass = this.themeService.isDarkMode() ? DARK_ICON_CLASS : LIGHT_ICON_CLASS;
  }
}
