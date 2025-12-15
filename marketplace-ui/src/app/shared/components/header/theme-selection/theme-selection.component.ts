import { Component, Inject } from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { NgClass, DOCUMENT } from '@angular/common';
import {
  LIGHT_ICON_CLASS,
  DARK_ICON_CLASS,
  DATA_THEME_ICON
} from '../../../constants/common.constant';

@Component({
  selector: 'app-theme-selection',
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
    this.iconClass = this.document.documentElement.getAttribute(DATA_THEME_ICON);
  }

  onToggleTheme(): void {
    this.themeService.changeTheme();
    if (this.themeService.isDarkMode()) {
      this.iconClass = DARK_ICON_CLASS;
    } else {
      this.iconClass = LIGHT_ICON_CLASS;
    }
  }
}
