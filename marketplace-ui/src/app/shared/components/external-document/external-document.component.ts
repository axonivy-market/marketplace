import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule } from '@ngx-translate/core';
import { ERROR_PAGE_PATH } from '../../constants/common.constant';
import { API_URI } from '../../constants/api.constant';
import { ExternalDocument } from '../../models/external-document.model';

const INDEX_FILE = '/index.html';

@Component({
  selector: 'app-external-document',
  standalone: true,
  imports: [TranslateModule],
  template: "<p>{{ 'common.labels.redirecting' | translate }}</p>"
})
export class ExternalDocumentComponent implements OnInit {
  httpClient = inject(HttpClient);

  constructor(private readonly activeRoute: ActivatedRoute,
    private readonly router: Router
  ) { }

  ngOnInit(): void {
    const product = this.activeRoute.snapshot.paramMap.get(ROUTER.ID);
    const version = this.activeRoute.snapshot.paramMap.get(ROUTER.VERSION);
    const currentUrl = window.location.href;

    if (product && version) {
      this.fetchDocumentUrl(product, version, currentUrl);
    }
  }

  fetchDocumentUrl(product: string, version: string, currentUrl: string): void {
    this.httpClient.get<ExternalDocument>(`${API_URI.EXTERNAL_DOCUMENT}/${product}/${version}`)
      .subscribe({
        next: (response: ExternalDocument) => this.handleRedirection(response, currentUrl)
      });
  }

  handleRedirection(response: ExternalDocument, currentUrl: string): void {
    if (response === null || response.relativeLink === '') {
      this.router.navigate([ERROR_PAGE_PATH]);
    }
    const relativeUrl = response.relativeLink;
    const isSameUrl = currentUrl === relativeUrl || currentUrl + INDEX_FILE === relativeUrl;
    if (!isSameUrl) {
      window.location.href = relativeUrl;
    }
  }
}
