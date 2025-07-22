import { Renderer2 } from '@angular/core';
import { DocumentRef } from '../../core/services/browser/document-ref.service';
import { WindowRef } from '../../core/services/browser/window-ref.service';
import {
  GOOGLE,
  GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_SOURCE,
  GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_TYPE,
  GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID,
  GOOGLE_SEARCH,
  GOOGLE_SEARCH_BAR_CLASS_NAME,
  GOOGLE_SEARCH_BAR_BACKGROUND_CLASS_NAME
} from '../constants/common.constant';

export class GoogleSearchBarUtils {
  static renderGoogleSearchBar(
    renderer: Renderer2,
    windowRef: WindowRef,
    documentRef: DocumentRef
  ): void {
    const doc = documentRef.nativeDocument;

    if (!doc) {
      return;
    }

    if (!doc.getElementById(GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID)) {
      const script = renderer.createElement('script');
      Object.assign(script, {
        id: GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID,
        type: GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_TYPE,
        async: true,
        src: GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_SOURCE,
        onload: () => this.addCustomClassToSearchBar(renderer, doc)
      });
      renderer.appendChild(doc.body, script);
    }

    const win = windowRef.nativeWindow;
    if (win?.hasOwnProperty(GOOGLE) && win.google?.search) {
      win.google.search.cse.element.render(GOOGLE_SEARCH);
    }
  }
  static addCustomClassToSearchBar(renderer: Renderer2, doc: Document): void {
    setTimeout(() => {
      const searchBoxList = doc.querySelectorAll(GOOGLE_SEARCH_BAR_CLASS_NAME);
      searchBoxList.forEach(searchBox =>
        renderer.addClass(searchBox, GOOGLE_SEARCH_BAR_BACKGROUND_CLASS_NAME)
      );
    }, 1000);
  }
}
