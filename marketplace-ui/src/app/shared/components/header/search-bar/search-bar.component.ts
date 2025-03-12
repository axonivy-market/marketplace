import { Event } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component, ComponentRef, ElementRef, HostListener, inject, Renderer2, signal, ViewChild, ViewContainerRef } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageSelectionComponent } from '../language-selection/language-selection.component';
import { ThemeSelectionComponent } from '../theme-selection/theme-selection.component';
import { LanguageService } from '../../../../core/services/language/language.service';
import { SEARCH_URL } from '../../../constants/common.constant';
import { FormsModule } from '@angular/forms';
import { GoogleSearchComponentComponent } from '../../google-search-component/google-search-component.component';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    ThemeSelectionComponent,
    FormsModule,
    LanguageSelectionComponent,
    GoogleSearchComponentComponent
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent {
  @ViewChild('divCreateText', { static: false }) divCreateText: ElementRef;
  searchUrl = SEARCH_URL;
  isSearchBarDisplayed = signal(false);
  isGoogleSearchBarDisplayed = signal(false);

  translateService = inject(TranslateService);
  elementRef = inject(ElementRef);
  languageService = inject(LanguageService);
  isGoogleLoaded = false;
  inputValue: string = '';

  constructor(private renderer: Renderer2) { }

  // ngAfterViewInit(): void {
  //   if (!document.getElementById('googleCSEScript')) {

  //     console.log("Main");
  //     const script = this.renderer.createElement('script');
  //     script.id = 'googleCSEScript';
  //     script.type = 'text/javascript';
  //     script.async = true;
  //     script.src = 'https://cse.google.com/cse.js?cx=036cea36d5dbf4f2b';
  //     console.log(script);

  //     this.renderer.appendChild(document.body, script);
  //   } 
  //   else {
  //     console.log("Else");

  //     if (window.hasOwnProperty('google') && (window as any).google.search) {
  //       (window as any).google.search.cse.element.render('gcse-search');
  //     }
  //   }
  // }

  // search(query: string): void {
  //   if (window.hasOwnProperty('google') && (window as any).google.search) {
  //     (window as any).google.search.cse.element.getElement('gcse-search').execute(query);
  //   }
  // }

  // ngAfterViewInit(): void {
  //   if (!document.getElementById('googleCSEScript')) {
  //     const script = this.renderer.createElement('script');
  //     script.id = 'googleCSEScript';
  //     script.type = 'text/javascript';
  //     script.async = true;
  //     script.src = 'https://cse.google.com/cse.js?cx=036cea36d5dbf4f2b';
  //     script.onload = () => {
  //       this.isGoogleLoaded = true; // Mark script as loaded
  //     };
  //     this.renderer.appendChild(document.body, script);
  //   } else {
  //     this.isGoogleLoaded = true;
  //   }
  // }

  // search(query: string): void {
  //   if (this.isGoogleLoaded && (window as any).google?.search?.cse) {
  //     const searchElement = (window as any).google.search.cse.element.getElement('gcse-search');
  //     if (searchElement) {
  //       searchElement.execute(query);
  //     } else {
  //       console.warn('Google Search Element not found.');
  //     }
  //   } else {
  //     console.warn('Google Search API is not ready yet.');
  //   }
  // }

  submitSearch(): void {
    console.log('Submit input:', this.inputValue);
    // this.search(this.inputValue);
    const button = document.querySelector('.gsc-search-button .gsc-search-button-v2') as HTMLButtonElement;
    const gscInput = document.querySelector('.gsc-input') as HTMLInputElement;
    gscInput.value = this.inputValue;
    console.log(button);
    console.log(gscInput.value);
    button.click();
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    // this.isGoogleSearchBarDisplayed.set(false);
    if (!this.elementRef.nativeElement.contains(event.target)) {
    this.isGoogleSearchBarDisplayed.set(false);
    this.isSearchBarDisplayed.set(false);
    }
  }

  onClickSearchIcon() {
    this.isGoogleSearchBarDisplayed.set(true);
    this.isSearchBarDisplayed.set(true);
  }

  onHideSearch() {
    this.isGoogleSearchBarDisplayed.set(false);
    this.isSearchBarDisplayed.set(false);
  }

  onClickSearchInput() {
    window.location.href = this.searchUrl;
  }

  onSearchInputChange() {
    console.log('Input changed:', this.inputValue);

  }
}
