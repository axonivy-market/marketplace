import { AfterViewInit, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from '../product.service';

@Component({
  selector: 'app-product-artifact-download',
  standalone: true,
  imports: [],
  template: '',
  providers: [ProductService]
})
export class ProductArtifactDownloadComponent implements AfterViewInit {
  ngAfterViewInit(): void {
    const productId = this.route.snapshot.params['id'];
    const version = this.route.snapshot.params['version'];
    const artifact = this.route.snapshot.params['artifact'];
    console.log(productId);
    console.log(version);
    console.log(artifact);
    this.productService
      .getLatestArtifactDownloadUrl(productId, version, artifact)
      .subscribe(data => {
        console.log(data);
        window.location.href = data;
      });
  }
  route = inject(ActivatedRoute);
  productService = inject(ProductService);

  ngOnInit(): void {
    const productId = this.route.snapshot.params['id'];
    const version = this.route.snapshot.params['version'];
    const artifact = this.route.snapshot.params['artifact'];
    console.log(productId);
    console.log(version);
    console.log(artifact);
    this.productService
      .getLatestArtifactDownloadUrl(productId, version, artifact)
      .subscribe(data => {
        console.log(data);
        window.location.href = data;
      });
  }
}
