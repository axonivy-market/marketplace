import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule } from '@ngx-translate/core';
import { ProductService } from '../../../modules/product/product.service';

@Component({
  selector: 'redirect-page',
  standalone: true,
  imports: [TranslateModule],
  template: "<p>{{ 'common.labels.redirecting' | translate }}</p>",
  providers: [ProductService]
})
export class RedirectPageComponent implements OnInit {
  productService = inject(ProductService);

  constructor(private readonly activedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    const product = this.activedRoute.snapshot.paramMap.get(ROUTER.ID);
    const version = this.activedRoute.snapshot.paramMap.get(ROUTER.VERSION);
    const artifact = this.activedRoute.snapshot.paramMap.get(ROUTER.ARTIFACT);
    
    if (product && version && artifact) {
      this.fetchLatestLibVersionDownloadUrl(product, version, artifact);
    }
  }

  fetchLatestLibVersionDownloadUrl( product: string, version: string, artifact: string): void {
    this.productService
      .getLatestArtifactDownloadUrl(product, version, artifact)
      .subscribe(downloadUrl => {
        window.location.href = downloadUrl;
      });
  }
}
