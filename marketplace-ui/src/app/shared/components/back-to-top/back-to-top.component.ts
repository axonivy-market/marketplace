import { CommonModule, DOCUMENT } from '@angular/common';
import { Component, HostListener, Inject } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-back-to-top',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
  ],
  templateUrl: './back-to-top.component.html',
  styleUrl: './back-to-top.component.scss'
})
export class BackToTopComponent {
  private readonly backToTopShowThreshold: number = 500;
  private readonly scrollBehavior: ScrollBehavior = 'smooth';
  showScrollButton: boolean = false;

  @HostListener("window:scroll", [])
  onWindowScroll() {
    if (window.scrollY >= this.backToTopShowThreshold || document.documentElement.scrollTop >= this.backToTopShowThreshold) {
      this.showScrollButton = true;
    }
    else {
      this.showScrollButton = false;
    }
  }

  scrollToTop() {
    if (this.showScrollButton) {
      window.scrollTo({ top: 0, behavior: this.scrollBehavior });
    }
    return;
  }
}
