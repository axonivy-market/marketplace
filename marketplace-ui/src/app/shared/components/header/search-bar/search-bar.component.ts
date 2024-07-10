import { CommonModule } from '@angular/common';
import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  inject,
  signal
} from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageSelectionComponent } from '../language-selection/language-selection.component';
import { ThemeSelectionComponent } from '../theme-selection/theme-selection.component';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    ThemeSelectionComponent,
    LanguageSelectionComponent
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent {
  @Input() isSearchBarDisplayed = signal(false);
  @Output() isShowSearchBarChange = new EventEmitter<boolean>();

  translateService = inject(TranslateService);

  elementRef = inject(ElementRef);

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isSearchBarDisplayed.set(false);
    }
  }

  onClickSearchIcon() {
    this.isSearchBarDisplayed.set(true);
  }

  onHideSearch() {
    this.isSearchBarDisplayed.set(false);
  }
}
