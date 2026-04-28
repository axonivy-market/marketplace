import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, Input, Output, EventEmitter, signal, OnChanges, SimpleChanges } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { SyncTaskKey } from '../admin-dashboard.service';
import { MarketProduct } from '../../../shared/models/product.model';

@Component({
  selector: 'app-sync-task-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './sync-task-dialog.component.html',
  styleUrls: ['./sync-task-dialog.component.scss']
})
export class SyncTaskDialogComponent implements OnChanges {
  @Input() visible = false;
  @Input() taskKey!: SyncTaskKey;
  @Input() products: MarketProduct[] = [];

  @Output() confirmSync = new EventEmitter<{
    productId: string;
    marketDirectory: string;
    override: boolean;
  }>();

  @Output() cancelSync = new EventEmitter<void>();

  productSearch = '';
  marketDirectory = '';
  overrideMarketItemPath = false;

  dropdownOpen = false;
  filteredProducts = signal<MarketProduct[]>([]);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['products']) {
      this.filteredProducts.set(this.products.slice(0, 10));
    }

    if (changes['visible']?.currentValue === true) {
      this.reset();
    }
  }

  getTaskConfig() {
    switch (this.taskKey) {
      case 'syncOneProduct':
        return {
          enableMarketPath: true,
          requireProduct: true
        };

      case 'syncZipArtifacts':
      default:
        return {
          enableMarketPath: false,
          requireProduct: true
        };
    }
  }

  isMarketPathEnabled(): boolean {
    return this.getTaskConfig().enableMarketPath;
  }

  openDropdown(): void {
    this.dropdownOpen = true;
    this.filteredProducts.set(this.products.slice(0, 10));
  }

  filterProducts(): void {
    const value = this.productSearch.toLowerCase();
    const filtered = this.products.filter(p => p.id.toLowerCase().includes(value)).slice(0, 10);
    this.filteredProducts.set(filtered);

    if (!this.products.some(p => p.id === this.productSearch)) {
      this.marketDirectory = '';
    }
    this.dropdownOpen = true;
  }

  selectProduct(product: MarketProduct): void {
    this.productSearch = product.id;
    this.marketDirectory = product.marketDirectory ?? '';
    this.dropdownOpen = false;
  }

  selectAllProducts(): void {
    this.productSearch = '';
    this.marketDirectory = '';
    this.dropdownOpen = false;
  }

  isValid(): boolean {
    const config = this.getTaskConfig();

    if (config.requireProduct) {
      if (!this.productSearch) {
        return true;
      }

      return this.products.some(p => p.id === this.productSearch) && !!this.marketDirectory;
    }

    return true;
  }

  onConfirm(): void {
    const config = this.getTaskConfig();

    if (config.requireProduct) {
      this.confirmSync.emit({
        productId: this.productSearch,
        marketDirectory: this.marketDirectory.trim(),
        override: this.overrideMarketItemPath
      });
    } else {
      this.confirmSync.emit({
        productId: '',
        marketDirectory: '',
        override: false
      });
    }
  }

  onCancel(): void {
    this.reset();
    this.cancelSync.emit();
  }

  isZipArtifacts(): boolean {
    return this.taskKey === 'syncZipArtifacts';
  }

  private reset(): void {
    this.productSearch = '';
    this.marketDirectory = '';
    this.overrideMarketItemPath = false;
    this.dropdownOpen = false;
  }
}
