import { Component, Inject, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HASH_SYMBOL } from '../../constants/common.constant';
import { API_URI } from '../../constants/api.constant';
import { ExternalDocument } from '../../models/external-document.model';
import { isPlatformBrowser } from '@angular/common';

const REDIRECTED_KEY = 'redirected';
@Component({
  selector: 'app-external-document',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './external-document.component.html'
})
export class ExternalDocumentComponent implements OnInit {
  httpClient = inject(HttpClient);
  translateService = inject(TranslateService);
  messageTitle = '';
  messageDetail = '';
  product = '';
  version = '';

  constructor(
    private readonly activedRoute: ActivatedRoute,
    private readonly router: Router,
    @Inject(PLATFORM_ID) private readonly platformId: Object
  ) { }

  ngOnInit(): void {
    this.translateService.get('common.labels.loading').subscribe(text => this.messageDetail = text);
    if (isPlatformBrowser(this.platformId)) {
      this.product = this.activedRoute.snapshot.paramMap.get(ROUTER.ID) ?? '';
      this.version = this.activedRoute.snapshot.paramMap.get(ROUTER.VERSION) ?? '';
      this.translateService.get('common.externalDocument.pageHeader',
        { product: this.product, version: this.version })
        .subscribe(text => this.messageTitle = text);

      const isRedirected = this.activedRoute.snapshot.queryParamMap.get(ROUTER.REDIRECTED);
      if (isRedirected === 'true') {
        return;
      }
      this.fetchDocumentUrl();
    }
  }

  fetchDocumentUrl(): void {
    this.httpClient.get<ExternalDocument>(`${API_URI.EXTERNAL_DOCUMENT}/${this.product}/${this.version}`)
      .subscribe({
        next: (response: ExternalDocument) => this.handleRedirection(response)
      });
  }

  handleRedirection(response: ExternalDocument): void {
    if (response === null || response.relativeLink === '') {
      this.translateService.get('common.externalDocument.helpText').subscribe(text => this.messageDetail = text);
      return;
    }
    const currentUrl = this.router.url;
    const relativeUrl = response.relativeLink;
    const isSameUrl = currentUrl === relativeUrl;
    const currentHash = window.location.hash;
    if (!isSameUrl) {
      let link = relativeUrl;
      if (!relativeUrl.includes(HASH_SYMBOL)) {
        link += currentHash;
      }
      window.location.href = `${link}?${REDIRECTED_KEY}=true`;
    }
  }
}
