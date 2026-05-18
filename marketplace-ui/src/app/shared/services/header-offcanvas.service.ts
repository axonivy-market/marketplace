import { Injectable, Renderer2, TemplateRef } from '@angular/core';
import { NgbOffcanvas, NgbOffcanvasRef } from '@ng-bootstrap/ng-bootstrap';
import { WindowRef } from '../../core/services/browser/window-ref.service';
import { DocumentRef } from '../../core/services/browser/document-ref.service';
import { GoogleSearchBarUtils } from '../utils/google-search-bar.utils';
import { HEADER_OFFCANVAS, HEADER_OFFCANVAS_BACKDROP, HEADER_OFFCANVAS_GOOGLE_SEARCH_BAR_ID, LARGE_BREAKPOINT } from '../constants/common.constant';

@Injectable({ providedIn: 'root' })
export class HeaderOffcanvasService {
  private offcanvasRef: NgbOffcanvasRef | null = null;

  constructor(
    private readonly offcanvas: NgbOffcanvas,
    private readonly windowRef: WindowRef,
    private readonly documentRef: DocumentRef
  ) {}

  open(content: TemplateRef<any>, renderer: Renderer2) {
    const doc = this.documentRef.nativeDocument;
    if (!doc) {
      return;
    }

    this.offcanvasRef = this.offcanvas.open(content, {
      ariaLabelledBy: HEADER_OFFCANVAS,
      backdrop: true,
      panelClass: HEADER_OFFCANVAS,
      position: 'end',
      backdropClass: HEADER_OFFCANVAS_BACKDROP
    });

    this.offcanvasRef.shown.subscribe(() => {
      GoogleSearchBarUtils.renderGoogleSearchBar(
        renderer,
        this.windowRef,
        this.documentRef,
        HEADER_OFFCANVAS_GOOGLE_SEARCH_BAR_ID
      );

      GoogleSearchBarUtils.addCustomClassToSearchBar(renderer, doc);
    });

    this.offcanvasRef.result.then(
      () => {
        this.offcanvasRef = null;
      },
      () => {
        this.offcanvasRef = null;
      }
    );
  }

  toggle(content: TemplateRef<any>, renderer: Renderer2) {
    if (window.innerWidth >= LARGE_BREAKPOINT) {
      return;
    }

    if (this.offcanvasRef) {
      this.close();
    } else {
      this.open(content, renderer);
    }
  }

  isOpen(): boolean {
    return !!this.offcanvasRef;
  }

  close() {
    this.offcanvasRef?.close();
    this.offcanvasRef = null;
  }

  handleResize() {
    if (window.innerWidth >= LARGE_BREAKPOINT && this.offcanvasRef) {
      this.close();
    }
  }
}
