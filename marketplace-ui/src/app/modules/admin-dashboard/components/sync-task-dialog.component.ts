import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, Input, Output, EventEmitter, signal, OnChanges, SimpleChanges } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { SyncTaskKey } from '../admin-dashboard.service';
import { MarketProduct } from '../../../shared/models/product.model';

type TaskKey = SyncTaskKey;

interface TaskDialogConfig {
  enableMarketPath: boolean;
  requireProduct: boolean;
  showAllOption: boolean;
  showOverrideCheckbox: boolean;
  dialogTitle: string;
}

const DEFAULT_TASK_CONFIG: TaskDialogConfig = {
  enableMarketPath: false,
  requireProduct: true,
  showAllOption: false,
  showOverrideCheckbox: false,
  dialogTitle: 'common.admin.sync.syncProductDialog.syncOneProductTitle'
};

const DIALOG_CONFIGS: Partial<Record<TaskKey, TaskDialogConfig>> = {
  syncOneProduct: {
    enableMarketPath: true,
    requireProduct: true,
    showAllOption: false,
    showOverrideCheckbox: true,
    dialogTitle: 'common.admin.sync.syncProductDialog.syncOneProductTitle'
  },
  syncZipArtifacts: {
    enableMarketPath: false,
    requireProduct: true,
    showAllOption: true,
    showOverrideCheckbox: false,
    dialogTitle: 'common.admin.sync.syncProductDialog.syncZIPArtifactTitle'
  }
};

@Component({
  selector: 'app-sync-task-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './sync-task-dialog.component.html',
  styleUrls: ['./sync-task-dialog.component.scss']
})
export class SyncTaskDialogComponent implements OnChanges {
  @Input() visible = false;
  @Input() taskKey!: TaskKey;
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

  currentConfig: TaskDialogConfig = DEFAULT_TASK_CONFIG;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['products']) {
      this.filteredProducts.set(this.products.slice(0, 10));
    }

    if (changes['taskKey'] && this.taskKey) {
      const cfg = DIALOG_CONFIGS[this.taskKey];
      this.currentConfig = cfg ?? DEFAULT_TASK_CONFIG;
    }

    if (changes['visible']?.currentValue === true) {
      this.reset();
      this.filteredProducts.set(this.products.slice(0, 10));
    }
  }

  openDropdown(): void {
    this.dropdownOpen = true;
    this.filteredProducts.set(this.products.slice(0, 10));
  }

  filterProducts(): void {
    const value = (this.productSearch ?? '').toLowerCase();
    const filtered = this.products.filter(p => p.id.toLowerCase().includes(value)).slice(0, 10);
    this.filteredProducts.set(filtered);

    const matches = this.products.some(p => p.id === (this.productSearch ?? ''));
    if (!matches && !this.currentConfig.showAllOption) {
      this.marketDirectory = '';
    }
    this.dropdownOpen = true;
  }

  selectProduct(product: MarketProduct): void {
    this.productSearch = product.id;
    this.marketDirectory = product.marketDirectory ?? '';
    this.dropdownOpen = false;
  }

  /**
   * Validation rules:
   * - If requireProduct is true:
   *   - Empty productSearch is valid only when showAllOption is true
   *   - Non-empty productSearch must match an existing product
   *   - If enableMarketPath is true, marketDirectory must be non-empty when a product is selected (or when required)
   * - If requireProduct is false: always valid
   */
  isValid(): boolean {
    const cfg = this.currentConfig;
    const productSearchTrim = (this.productSearch ?? '').trim();
    const marketDirectoryTrim = (this.marketDirectory ?? '').trim();

    if (cfg.requireProduct) {
      if (productSearchTrim.length === 0) {
        return !!cfg.showAllOption;
      }

      const productMatch = this.products.some(p => p.id === productSearchTrim);
      if (!productMatch) {
        return false;
      }

      if (cfg.enableMarketPath) {
        return marketDirectoryTrim.length > 0;
      }

      return true;
    }

    return true;
  }

  onConfirm(): void {
    const productId = (this.productSearch ?? '').trim();
    const marketDirectory = (this.marketDirectory ?? '').trim();

    this.confirmSync.emit({
      productId,
      marketDirectory,
      override: this.overrideMarketItemPath
    });
  }

  onCancel(): void {
    this.reset();
    this.cancelSync.emit();
  }

  private reset(): void {
    this.productSearch = '';
    this.marketDirectory = '';
    this.overrideMarketItemPath = false;
    this.dropdownOpen = false;
  }
}