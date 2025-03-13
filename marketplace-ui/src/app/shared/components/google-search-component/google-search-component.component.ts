import { CommonModule } from '@angular/common';
import { Component, ElementRef, Renderer2 } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeSelectionComponent } from '../header/theme-selection/theme-selection.component';
import { FormsModule } from '@angular/forms';
import { LanguageSelectionComponent } from '../header/language-selection/language-selection.component';

@Component({
  selector: 'app-google-search-component',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    FormsModule
  ],
  templateUrl: './google-search-component.component.html',
  styleUrl: './google-search-component.component.scss'
})
export class GoogleSearchComponentComponent {
  
  constructor(private renderer: Renderer2, private el: ElementRef) { }

  ngAfterViewInit(): void {
    if (!document.getElementById('googleCSEScript')) {
      console.log("Main case");
      
      const script = this.renderer.createElement('script');
      script.id = 'googleCSEScript';
      script.type = 'text/javascript';
      script.async = true;
      script.src = 'https://cse.google.com/cse.js?cx=036cea36d5dbf4f2b';
      script.onload = () => {
        this.addCustomClassToSearchBar();
      };
      this.renderer.appendChild(document.body, script);
    } 
    else {
      console.log("Else case");
      // If script is already loaded, manually trigger reinitialization
      if (window.hasOwnProperty('google') && (window as any).google.search) {
        (window as any).google.search.cse.element.render('gcse-search');
      }
    }
  }

  private addCustomClassToSearchBar(): void {
    setTimeout(() => {
      const searchBox = document.querySelector('.gsc-control-cse'); // Google's search bar container
      if (searchBox) {
        // this.renderer.addClass(searchBox, 'bg-secondary');
      }
    }, 1000); // Give Google time to load
  }
}
