import { Component, Inject } from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { NgClass, DOCUMENT } from '@angular/common';
import {
  THEME_ICON_ATTR,
  LIGHT_ICON_CLASS,
  DARK_ICON_CLASS
} from '../../../../shared/constants/common.constant';

@Component({
  selector: 'app-theme-selection',
  standalone: true,
  imports: [NgClass],
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
      this.document.documentElement.getAttribute(THEME_ICON_ATTR);
  }

  onToggleTheme(): void {
    this.themeService.changeTheme();
    this.iconClass = this.themeService.isDarkMode() ? DARK_ICON_CLASS : LIGHT_ICON_CLASS;
  }
}
