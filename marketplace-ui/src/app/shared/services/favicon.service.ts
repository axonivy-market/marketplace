import { Inject, Injectable, DOCUMENT } from "@angular/core";
import { LINK_REL_ICON, LINK_REL_QUERY } from "../constants/common.constant";

@Injectable({
  providedIn: 'root'
})
export class FaviconService {
  constructor(@Inject(DOCUMENT) private readonly document: Document) {}
  setFavicon(url: string): void {
    let link: HTMLLinkElement | null = this.document.querySelector(LINK_REL_QUERY);

    if (!link) {
      link = this.document.createElement('link');
      link.rel = LINK_REL_ICON;
      this.document.head.appendChild(link);
    }
    link.href = url;
  }
}