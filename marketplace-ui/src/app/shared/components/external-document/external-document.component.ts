import { Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HASH_SYMBOL } from '../../constants/common.constant';
import { API_URI } from '../../constants/api.constant';
import { ExternalDocument } from '../../models/external-document.model';

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
  messageTitle: string = '';
  messageDetail: string = '';
  product: string = '';
  version: string = '';

  constructor(
    private readonly activedRoute: ActivatedRoute,
    private readonly router: Router
  ) { }

  ngOnInit(): void {
    this.translateService.get('common.labels.loading').subscribe(value => this.messageDetail = value);
    this.product = this.activedRoute.snapshot.paramMap.get(ROUTER.ID) ?? '';
    this.version = this.activedRoute.snapshot.paramMap.get(ROUTER.VERSION) ?? '';
    this.translateService.get('common.externalDocument.pageHeader',
      { product: this.product, version: this.version })
      .subscribe(value => this.messageTitle = value);
    const isRedirected = this.activedRoute.snapshot.queryParamMap.get(ROUTER.REDIRECTED);
    if (isRedirected === 'true') {
      return;
    }
    this.fetchDocumentUrl();
  }

  fetchDocumentUrl(): void {
    this.httpClient.get<ExternalDocument>(`${API_URI.EXTERNAL_DOCUMENT}/${this.product}/${this.version}`)
      .subscribe({
        next: (response: ExternalDocument) => this.handleRedirection(response)
      });
  }

  handleRedirection(response: ExternalDocument): void {
    if (response === null || response.relativeLink === '') {
      this.translateService.get('common.externalDocument.helpText').subscribe(value => this.messageDetail = value);
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
