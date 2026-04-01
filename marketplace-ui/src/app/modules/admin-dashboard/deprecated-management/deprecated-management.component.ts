import { Component, inject } from '@angular/core';
import { AsyncPipe, NgClass } from '@angular/common';

import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { CustomSortCardComponent } from '../custom-sort/custom-sort-card/custom-sort-card.component';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { firstValueFrom } from 'rxjs';
import { ProductService } from '../../product/product.service';
import { DeprecatedRequest } from '../../../shared/models/deprecated-request';

@Component({
  selector: 'app-deprecated-management',
  imports: [
    AsyncPipe,
    CustomSortCardComponent,
    FormsModule,
    TranslateModule,
    NgClass
  ],
  templateUrl: './deprecated-management.component.html',
  styleUrl: './deprecated-management.component.scss'
})
export class DeprecatedManagementComponent {
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  themeService = inject(ThemeService);
  showDeprecatedProductDialog = false;

  private readonly productService = inject(ProductService);

  dropdownOpen = false;
  deprecatedItems: DeprecatedRequest = {
    productId: '',
    successorUrl: '',
    addReadme: false,
    deprecated: false
  };
  selectableProductIds: string[] = [];
  deprecatedProductIds: string[] = [];

  // Validation state
  validationErrors: { productId?: string; successorUrl?: string } = {};

  async ngOnInit(): Promise<void> {
    this.deprecatedProductIds = await this.loadAllProductIds(true);
  }

  trigger() {
    this.showDeprecatedProductDialog = true;
  }

  openDialog(): void {
    this.deprecatedProductIds = this.deprecatedProductIds.slice(0, 10);
  }

  closeDialog() {
    this.showDeprecatedProductDialog = false;
  }

  async openExtensionDropdown() {
    this.selectableProductIds = await this.loadAllProductIds(null);
    this.dropdownOpen = true;
  }

  async deprecatedProduct() {
    // Validate inputs
    if (!this.validateForm()) {
      return;
    }

    this.deprecatedProductIds = await firstValueFrom(
      this.productService.updateDeprecatedProduct(this.deprecatedItems)
    );
    this.closeDialog();
    this.deprecatedItems = {
      productId: '',
      successorUrl: '',
      addReadme: false,
      deprecated: false
    };
    this.validationErrors = {};
  }

  validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;

    // Validate productId (required)
    if (!this.deprecatedItems.productId || this.deprecatedItems.productId.trim() === '') {
      this.validationErrors['productId'] = 'Extension ID is required';
      isValid = false;
    }

    // Validate successorUrl (optional but must match pattern if provided)
    if (this.deprecatedItems.successorUrl && this.deprecatedItems.successorUrl.trim() !== '') {
      const urlPattern = /^(http|https):\/\/.*$/;
      if (!urlPattern.test(this.deprecatedItems.successorUrl)) {
        this.validationErrors['successorUrl'] = 'URL must start with http:// or https://';
        isValid = false;
      }
    }

    return isValid;
  }

  selectExtension(productId: string) {
    this.deprecatedItems.productId = productId;
    this.deprecatedItems.deprecated = true;
  }

  private async loadAllProductIds(
    predicated: Boolean | null
  ): Promise<string[]> {
    return await firstValueFrom(
      this.productService.fetchAllProductIdsByDeprecated(predicated)
    );
  }
}
