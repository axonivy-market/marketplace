import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule } from '@ngx-translate/core';
import { ERROR_PAGE_PATH, NOT_FOUND_ERROR_CODE } from '../../constants/common.constant';

const INDEX_FILE = '/index.html';
const DOC_API = 'api/externaldocument';

@Component({
  selector: 'app-external-document',
  standalone: true,
  imports: [TranslateModule],
  template: "<p>{{ 'common.labels.redirecting' | translate }}</p>"
})
export class ExternalDocumentComponent implements OnInit {
  httpClient = inject(HttpClient);

  constructor(private activeRoute: ActivatedRoute,
    private readonly router: Router
  ) { }

  ngOnInit(): void {
    const product = this.activeRoute.snapshot.paramMap.get(ROUTER.ID);
    const version = this.activeRoute.snapshot.paramMap.get(ROUTER.VERSION);
    const currentUrl = window.location.href;

    if (product && version) {
      this.fetchDocumentUrl(product, version, currentUrl);
    } else {
      console.error('Product or version is missing from route parameters');
    }
  }

  fetchDocumentUrl(product: string, version: string, currentUrl: string): void {
    this.httpClient.get<string>(`${DOC_API}/${product}/${version}`)
      .subscribe({
        next: (response: string) => this.handleRedirection(response, currentUrl),
        error: (error) => console.error('Error fetching document URL:', error)
      });
  }

  handleRedirection(response: string, currentUrl: string): void {
    if (response === null || response === '') {
      this.router.navigate([ERROR_PAGE_PATH]);
    }

    const isSameUrl = currentUrl === response || currentUrl + INDEX_FILE === response;
    if (isSameUrl) {
      console.log('No redirection needed, the URLs are the same.');
    } else {
      window.location.href = response;
    }
  }
}
