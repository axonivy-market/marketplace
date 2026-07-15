import { Component, effect, inject, Input, Signal, signal, ChangeDetectorRef, NgZone } from '@angular/core';
import { TranslateModule } from "@ngx-translate/core";
import { LanguageService } from '../../../../core/services/language/language.service';
import { ProductService } from '../../product.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-product-installation-count-action',
  imports: [TranslateModule],
  templateUrl: './product-installation-count-action.component.html',
  styleUrl: './product-installation-count-action.component.scss'
})
export class ProductInstallationCountActionComponent {
  @Input({ required: true }) productId!: string;
  @Input({ required: true }) refreshInstallationCount!: Signal<number>;
  currentInstallationCount = signal<number>(0);

  languageService = inject(LanguageService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly ngZone = inject(NgZone);

  private handleInstallationCount = (data: number) => {
    this.ngZone.run(() => {
      this.currentInstallationCount.set(data);
      this.cdr.markForCheck();
    });
  };

  constructor(private readonly productService: ProductService) {
    effect((onCleanup) => {
      this.refreshInstallationCount();

      let sub: Subscription | undefined;
      const timer = setTimeout(() => {
        sub = this.productService
          .sendRequestToGetInstallationCount(this.productId)
          .subscribe(this.handleInstallationCount);
      }, 100);

      onCleanup(() => {
        clearTimeout(timer);
        sub?.unsubscribe();
      });
    });
  }
}
