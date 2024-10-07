import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule } from '@ngx-translate/core';
import { ERROR_PAGE_PATH } from '../../constants/common.constant';
import { ProductService } from '../../../modules/product/product.service';

const INDEX_FILE = '/index.html';
const DOC_API = 'api/externaldocument';

@Component({
  selector: 'redirect-page',
  standalone: true,
  imports: [TranslateModule],
  template: "<p>{{ 'common.labels.redirecting' | translate }}</p>",
  providers: [ProductService]
})
export class RedirectPageComponent implements OnInit {
  httpClient = inject(HttpClient);
  productService = inject(ProductService);

  constructor(
    private readonly activeRoute: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const product = this.activeRoute.snapshot.paramMap.get(ROUTER.ID);
    const version = this.activeRoute.snapshot.paramMap.get(ROUTER.VERSION);
    const currentUrl = window.location.href;
    const artifact = this.activeRoute.snapshot.paramMap.get(ROUTER.ARTIFACT);
    const fileType = this.activeRoute.snapshot.paramMap.get(ROUTER.FILE_TYPE);

    if (product && version) {
      if ( artifact && fileType) {
        this.fetchLatestLibVersionDownloadUrl(product, version, artifact, fileType, currentUrl);
        return;
      }
      this.fetchDocumentUrl(product, version, currentUrl);
    }
  }

  fetchDocumentUrl(product: string, version: string, currentUrl: string): void {
    this.httpClient.get<string>(`${DOC_API}/${product}/${version}`).subscribe({
      next: (response: string) => this.handleRedirection(response, currentUrl)
    });
  }

  fetchLatestLibVersionDownloadUrl(
    product: string,
    version: string,
    artifact: string,
    fileType: string,
    currentUrl: string
  ): void {
    this.productService
      .getLatestArtifactDownloadUrl(product, version, artifact, fileType)
      .subscribe(downloadUrl => {
        this.handleRedirection(downloadUrl, currentUrl);
      });
  }

  handleRedirection(response: string, currentUrl: string): void {
    if (response === null || response === '') {
      this.router.navigate([ERROR_PAGE_PATH]);
    }
    const isSameUrl = currentUrl === response || currentUrl + INDEX_FILE === response;
    if (!isSameUrl) {
      window.location.href = response;
    }
  }
}
