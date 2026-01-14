import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-back-to-top',
  imports: [
    CommonModule,
    TranslateModule,
  ],
  templateUrl: './back-to-top.component.html',
  styleUrl: './back-to-top.component.scss'
})
export class BackToTopComponent {
  showScrollButton = false;
  backToTopShowThreshold = 500;
  scrollBehavior: ScrollBehavior = 'smooth';

  @HostListener("window:scroll", [])
  onWindowScroll() {
    const isWindowScrollTopOverThreshold = window.scrollY >= this.backToTopShowThreshold;
    const isDocumentScrollTopOverThreshold = document.documentElement.scrollTop >= this.backToTopShowThreshold;
    this.showScrollButton = isWindowScrollTopOverThreshold || isDocumentScrollTopOverThreshold;
  }

  scrollToTop() {
    if (this.showScrollButton) {
      window.scrollTo({ top: 0, behavior: this.scrollBehavior });
    }
  }
}
