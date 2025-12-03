import { DOCUMENT } from "@angular/common";
import { Inject, Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class FaviconService {
  constructor(@Inject(DOCUMENT) private document: Document) {}
  setFavicon(url: string): void {
    let link: HTMLLinkElement | null = this.document.querySelector('link[rel="icon"]');

    if (!link) {
      link = this.document.createElement('link');
      link.rel = 'icon';
      link.type = 'image/x-icon'; // Or 'image/png', 'image/gif' based on your favicon type
      this.document.head.appendChild(link);
    }
    link.href = url;
  }
}