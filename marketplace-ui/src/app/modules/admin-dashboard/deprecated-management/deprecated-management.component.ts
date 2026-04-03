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
import { PullRequestAction } from '../../../shared/enums/pullrequest-action';
import { DeprecatedResponse } from '../../../shared/models/deprecated-response';

@Component({
  selector: 'app-deprecated-management',
  imports: [
    AsyncPipe,
    CustomSortCardComponent,
    FormsModule,
    TranslateModule
  ],
  templateUrl: './deprecated-management.component.html',
  styleUrl: './deprecated-management.component.scss'
})
export class DeprecatedManagementComponent {
  productService = inject(ProductService);
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  themeService = inject(ThemeService);

  // Undeprecate confirm dialog state
  showUndeprecateConfirmDialog = false;
  isClosingUndeprecateDialog = false;
  showDeprecatedProductDialog = false;
  isClosing = false;
  isDeprecating = false;
  isCopySuccessVisible = false;
  undeprecateProductId = '';

  dropdownOpen = false;
  deprecatedItems: DeprecatedRequest = {
    productId: '',
    successorUrl: '',
    addReadme: false,
    deprecated: false,
    pullRequestAction: PullRequestAction.ADD
  };
  selectableProductIds: string[] = [];
  filteredProductIds: string[] = [];
  deprecatedProductIds: string[] = [];
  deprecatedResponse: DeprecatedResponse = {
    productIds: [],
    pullRequestUrl: ''
  };

  // Validation state
  validationErrors: { productId?: string; successorUrl?: string } = {};

  async ngOnInit(): Promise<void> {
    this.deprecatedProductIds = await this.loadAllProductIds(true);
  }

  trigger() {
    this.showDeprecatedProductDialog = true;
    this.isCopySuccessVisible = false;
    this.deprecatedResponse.pullRequestUrl = '';
  }

  openDialog(): void {
    this.deprecatedProductIds = this.deprecatedProductIds.slice(0, 10);
  }

  closeDialog() {
    this.isClosing = true;
    setTimeout(() => {
      this.showDeprecatedProductDialog = false;
      this.isClosing = false;
      this.isDeprecating = false;
      this.isCopySuccessVisible = false;
      this.deprecatedItems = {
        productId: '',
        successorUrl: '',
        addReadme: false,
        deprecated: false
      };
      this.validationErrors = {};
    }, 250);
  }

  async openExtensionDropdown() {
    this.selectableProductIds = await this.loadAllProductIds(null);
    this.filterProducts(this.deprecatedItems.productId);
    this.dropdownOpen = true;
  }

  async deprecatedProduct() {
    if (this.isDeprecating || !!this.deprecatedResponse.pullRequestUrl) {
      return;
    }

    // Validate inputs
    if (!this.validateForm()) {
      return;
    }
    this.isDeprecating = true;
    this.isCopySuccessVisible = false;
    this.dropdownOpen = false;

    try {
      this.deprecatedResponse = await firstValueFrom(
        this.productService.updateDeprecatedProduct(this.deprecatedItems)
      );
      this.deprecatedProductIds = this.deprecatedResponse.productIds;
      this.validationErrors = {};
    } finally {
      this.isDeprecating = false;
    }
  }

  async copyPullRequestUrl(): Promise<void> {
    if (!this.deprecatedResponse.pullRequestUrl || !navigator?.clipboard) {
      return;
    }

    await navigator.clipboard.writeText(this.deprecatedResponse.pullRequestUrl);
    this.isCopySuccessVisible = true;
    setTimeout(() => {
      this.isCopySuccessVisible = false;
    }, 1500);
  }

  validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;

    // Validate productId (required)
    if (
      !this.deprecatedItems.productId ||
      this.deprecatedItems.productId.trim() === ''
    ) {
      this.validationErrors['productId'] = 'Extension ID is required';
      isValid = false;
    }

    // Validate successorUrl (optional but must match pattern if provided)
    if (
      this.deprecatedItems.successorUrl &&
      this.deprecatedItems.successorUrl.trim() !== ''
    ) {
      const urlPattern = /^(http|https):\/\/.*$/;
      if (!urlPattern.test(this.deprecatedItems.successorUrl)) {
        this.validationErrors['successorUrl'] =
          'URL must start with http:// or https://';
        isValid = false;
      }
    }

    return isValid;
  }

  selectExtension(productId: string) {
    this.deprecatedItems.productId = productId;
    this.deprecatedItems.deprecated = true;
    this.dropdownOpen = false;
    this.deprecatedItems.pullRequestAction = PullRequestAction.ADD;
  }

  async confirmUndeprecate(productId: string): Promise<void> {
    this.undeprecateProductId = productId;
    this.showUndeprecateConfirmDialog = true;
  }

  closeUndeprecateDialog(): void {
    this.isClosingUndeprecateDialog = true;
    setTimeout(() => {
      this.showUndeprecateConfirmDialog = false;
      this.isClosingUndeprecateDialog = false;
      this.undeprecateProductId = '';
    }, 250);
  }

  async executeUndeprecate(): Promise<void> {
    const request: DeprecatedRequest = {
      productId: this.undeprecateProductId,
      successorUrl: '',
      addReadme: false,
      deprecated: false,
      pullRequestAction: PullRequestAction.REMOVE
    };
    this.deprecatedResponse = await firstValueFrom(
      this.productService.updateDeprecatedProduct(request)
    );
    this.deprecatedProductIds = this.deprecatedResponse.productIds;
    this.closeUndeprecateDialog();
  }

  filterProducts(searchTerm: string) {
    const normalized = (searchTerm || '').trim().toLowerCase();
    if (!normalized) {
      this.filteredProductIds = [...this.selectableProductIds];
      return;
    }

    this.filteredProductIds = this.selectableProductIds.filter(productId =>
      productId.toLowerCase().includes(normalized)
    );
  }

  private async loadAllProductIds(
    predicated: Boolean | null
  ): Promise<string[]> {
    return await firstValueFrom(
      this.productService.fetchAllProductIdsByDeprecated(predicated)
    );
  }
}
