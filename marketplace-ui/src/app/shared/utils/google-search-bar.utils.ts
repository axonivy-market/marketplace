import { Renderer2 } from '@angular/core';
import { environment } from '../../../environments/environment';
import { DocumentRef } from '../../core/services/browser/document-ref.service';
import { WindowRef } from '../../core/services/browser/window-ref.service';

// export class GoogleSearchBarUtils {
//   static renderGoogleSearchBar(renderer: Renderer2): void {
//     if (!document.getElementById('googleCSEScript')) {
//       const script = renderer.createElement('script');
//       Object.assign(script, {
//         id: environment.googleProgrammableSearchScriptId,
//         type: environment.googleProgrammableSearchScriptType,
//         async: true,
//         src: environment.googleProgrammableSearchScriptSource,
//         onload: () => this.addCustomClassToSearchBar(renderer)
//       });
//       renderer.appendChild(document.body, script);
//     }
//     // If script is already loaded, manually trigger reinitialization
//     if (window.hasOwnProperty('google') && window.google.search) {
//       window.google.search.cse.element.render('gcse-search');
//     }
//   }

//   static addCustomClassToSearchBar(renderer: Renderer2): void {
//     setTimeout(() => {
//       const searchBoxList = document.querySelectorAll('.gsc-control-cse');
//       searchBoxList.forEach(searchBox => renderer.addClass(searchBox, 'bg-secondary'));
//     }, 1000); // Give Google time to load
//   }
// }
export class GoogleSearchBarUtils {
  static renderGoogleSearchBar(
    renderer: Renderer2,
    windowRef: WindowRef,
    documentRef: DocumentRef
  ): void {
    const win = windowRef.nativeWindow;
    const doc = documentRef.nativeDocument;

    if (!doc) return;

    if (!doc.getElementById('googleCSEScript')) {
      const script = renderer.createElement('script');
      Object.assign(script, {
        id: environment.googleProgrammableSearchScriptId,
        type: environment.googleProgrammableSearchScriptType,
        async: true,
        src: environment.googleProgrammableSearchScriptSource,
        onload: () => this.addCustomClassToSearchBar(renderer, documentRef)
      });
      renderer.appendChild(doc.body, script);
    }

    if (win?.hasOwnProperty('google') && win.google?.search) {
      win.google.search.cse.element.render('gcse-search');
    }
  }

  static addCustomClassToSearchBar(
    renderer: Renderer2,
    documentRef: DocumentRef
  ): void {
    const doc = documentRef.nativeDocument;
    if (!doc) return;

    setTimeout(() => {
      const searchBoxList = doc.querySelectorAll('.gsc-control-cse');
      searchBoxList.forEach(searchBox =>
        renderer.addClass(searchBox, 'bg-secondary')
      );
    }, 1000);
  }
}