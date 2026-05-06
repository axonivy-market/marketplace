import { Renderer2 } from '@angular/core';
import { DocumentRef } from '../../core/services/browser/document-ref.service';
import { WindowRef } from '../../core/services/browser/window-ref.service';
import {
  GOOGLE,
  GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_SOURCE,
  GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_TYPE,
  GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID,
  GOOGLE_SEARCH,
  GOOGLE_SEARCH_BAR_BACKGROUND_CLASS_NAME,
  GOOGLE_SEARCH_BAR_CLASS_NAME,
  GOOGLE_SEARCH_BAR_INPUT_CLASS_NAME,
  GOOGLE_SEARCH_BAR_PRIMARY_TEXT_CLASS_NAME
} from '../constants/common.constant';

const GOOGLE_SEARCH_BAR_DELAY_RENDERING_TIME = 500;

export class GoogleSearchBarUtils {
  static renderGoogleSearchBar(
    renderer: Renderer2,
    windowRef: WindowRef,
    documentRef: DocumentRef,
    identifier: string = GOOGLE_SEARCH
  ): void {
    const doc = documentRef.nativeDocument;

    if (!doc) {
      return;
    }

    if (!doc.getElementById(GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID)) {
      const script = renderer.createElement('script');
      Object.assign(script, {
        id: GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID,
        type: GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_TYPE,
        async: true,
        src: GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_SOURCE,
        onload: () => this.addCustomClassToSearchBar(renderer, doc)
      });
      renderer.appendChild(doc.body, script);
    }

    const win = windowRef.nativeWindow;
    if (win?.hasOwnProperty(GOOGLE) && win.google?.search) {
      if (identifier === GOOGLE_SEARCH) {
        win.google.search.cse.element.render(identifier);
      } else {
        win.google.search.cse.element.render({
          div: identifier
        });
      }
    }
  }
  static addCustomClassToSearchBar(renderer: Renderer2, doc: Document): void {
    setTimeout(() => {
      this.addBackgroundClassToSearchBar(renderer, doc);
      this.addTextPrimaryClassToSearchInput(renderer, doc);
    }, GOOGLE_SEARCH_BAR_DELAY_RENDERING_TIME);
  }

  private static addBackgroundClassToSearchBar(
    renderer: Renderer2,
    doc: Document
  ): void {
    const searchBoxList = doc.querySelectorAll(GOOGLE_SEARCH_BAR_CLASS_NAME);
    searchBoxList.forEach(searchBox =>
      renderer.addClass(searchBox, GOOGLE_SEARCH_BAR_BACKGROUND_CLASS_NAME)
    );
  }

  private static addTextPrimaryClassToSearchInput(
    renderer: Renderer2,
    doc: Document
  ): void {
    const googleSearchInputList = doc.querySelectorAll(
      GOOGLE_SEARCH_BAR_INPUT_CLASS_NAME
    );
    googleSearchInputList.forEach(input =>
      renderer.addClass(input, GOOGLE_SEARCH_BAR_PRIMARY_TEXT_CLASS_NAME)
    );
  }
}
