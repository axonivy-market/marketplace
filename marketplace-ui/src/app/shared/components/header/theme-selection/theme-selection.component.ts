import { Component, Inject } from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { NgClass, DOCUMENT } from '@angular/common';
import {
  LIGHT_ICON_CLASS,
  DARK_ICON_CLASS,
  DATA_THEME
} from '../../../../shared/constants/common.constant';
import { Theme } from '../../../enums/theme.enum';

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
    const theme = this.document.defaultView?.localStorage.getItem(DATA_THEME) as Theme;
    if (theme === Theme.DARK) {
      this.iconClass = DARK_ICON_CLASS;
    } else {
      this.iconClass = LIGHT_ICON_CLASS;
    }
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
