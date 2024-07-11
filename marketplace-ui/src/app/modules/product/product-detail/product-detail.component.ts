import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Product } from '../../../shared/models/product.model';
import { ProductService } from '../product.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { ProductDetailVersionActionComponent } from './product-detail-version-action/product-detail-version-action.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [MultilingualismPipe, ProductDetailVersionActionComponent],
  providers: [ProductService],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent {
  product!: Product;
  route = inject(ActivatedRoute);
  productService = inject(ProductService);
  languageService = inject(LanguageService);
  productId!: string;

  constructor() {
    const productId = this.route.snapshot.params['id'];
    if (productId) {
      this.productId = productId;
      this.productService.getProductById(productId).subscribe(product => {
        this.product = product;
      });
    }
  }
}