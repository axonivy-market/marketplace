import { Component, effect, inject, Input, Signal, signal } from '@angular/core';
import { TranslateModule } from "@ngx-translate/core";
import { LanguageService } from '../../../../core/services/language/language.service';
import { ProductService } from '../../product.service';

@Component({
  selector: 'app-product-installation-count-action',
  standalone: true,
  imports: [
    TranslateModule
  ],
  templateUrl: './product-installation-count-action.component.html',
  styleUrl: './product-installation-count-action.component.scss'
})

export class ProductInstallationCountActionComponent {
  @Input({ required: true }) productId!: string;
  @Input({ required: true }) refreshInstallationCount!: Signal<number>;
  currentInstallationCount = signal<number>(0);

  languageService = inject(LanguageService);

  constructor(private readonly productService: ProductService) {
    effect(() => {
      this.refreshInstallationCount();
      setTimeout(() => {
        this.productService.sendRequestToGetInstallationCount(this.productId).subscribe(
          (data: number) => {
            this.currentInstallationCount.set(data);
          });
      }, 100);
    });
  }
}
