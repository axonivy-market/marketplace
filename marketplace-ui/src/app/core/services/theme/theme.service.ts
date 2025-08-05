import { DOCUMENT } from '@angular/common';
import { Inject, Injectable, signal, WritableSignal } from '@angular/core';
import { Theme } from '../../../shared/enums/theme.enum';

const DATA_THEME = 'data-bs-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  isDarkMode: WritableSignal<boolean>;
  theme: WritableSignal<Theme>;

  constructor(@Inject(DOCUMENT) private readonly document: Document) {
    const html = this.document.documentElement;
    const storage = this.document.defaultView?.localStorage;

    let initialTheme: Theme = Theme.LIGHT;

    if (storage) {
      const saved = storage.getItem(DATA_THEME) as Theme;
      if (saved === Theme.DARK || saved === Theme.LIGHT) {
        initialTheme = saved;
      } else {
        // fallback to system preference
        const prefersDark = this.document.defaultView?.matchMedia?.('(prefers-color-scheme: dark)').matches;
        if (prefersDark) initialTheme = Theme.DARK;
      }
    }

    this.theme = signal(initialTheme);
    this.isDarkMode = signal(initialTheme === Theme.DARK);

    html.setAttribute(DATA_THEME, initialTheme);
  }

  setTheme(theme: Theme): void {
    const html = this.document.documentElement;
    const storage = this.document.defaultView?.localStorage;

    html.setAttribute(DATA_THEME, theme);
    if (storage) storage.setItem(DATA_THEME, theme);

    this.theme.set(theme);
    this.isDarkMode.set(theme === Theme.DARK);
  }

  changeTheme(): void {
    this.setTheme(this.theme() === Theme.DARK ? Theme.LIGHT : Theme.DARK);
  }
}