import { DOCUMENT } from "@angular/common";
import { Inject, Injectable } from "@angular/core";
import { FAVICON_LINK_REL_QUERY, FAVICON_REL } from "../constants/common.constant";

@Injectable({
  providedIn: 'root'
})
export class FaviconService {
  constructor(@Inject(DOCUMENT) private readonly document: Document) {}
  setFavicon(url: string, type: string): void {
    let link: HTMLLinkElement | null = this.document.querySelector(FAVICON_LINK_REL_QUERY);

    if (!link) {
      link = this.document.createElement('link');
      link.rel = FAVICON_REL;
      this.document.head.appendChild(link);
    }
    link.href = url;
    link.type = type;
  }
}