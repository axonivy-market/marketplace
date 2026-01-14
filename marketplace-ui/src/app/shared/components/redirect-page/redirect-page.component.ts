import { Component, Inject, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule } from '@ngx-translate/core';
import { ProductService } from '../../../modules/product/product.service';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'redirect-page',
  imports: [TranslateModule],
  template: "<p>{{ 'common.labels.redirecting' | translate }}</p>",
  providers: [ProductService]
})
export class RedirectPageComponent implements OnInit {
  productService = inject(ProductService);

  constructor(
    private readonly activeRoute: ActivatedRoute,
    @Inject(PLATFORM_ID) private readonly platformId: Object
  ) { }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const product = this.activeRoute.snapshot.paramMap.get(ROUTER.ID);
      const version = this.activeRoute.snapshot.paramMap.get(ROUTER.VERSION);
      const artifact = this.activeRoute.snapshot.paramMap.get(ROUTER.ARTIFACT);
      if (product && version && artifact) {
        this.fetchLatestLibVersionDownloadUrl(product, version, artifact);
      }
    }
  }

  fetchLatestLibVersionDownloadUrl(product: string, version: string, artifact: string): void {
    this.productService.getLatestArtifactDownloadUrl(product, version, artifact)
      .subscribe(downloadUrl => {
        window.location.href = downloadUrl;
      });
  }
}
