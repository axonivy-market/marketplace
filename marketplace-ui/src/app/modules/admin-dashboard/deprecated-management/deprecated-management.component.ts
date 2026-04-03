import { Component, inject, OnInit } from '@angular/core';
import { AsyncPipe, DatePipe, NgOptimizedImage } from '@angular/common';

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
import { DeprecatedProductInfo } from '../../../shared/models/deprecated-product-info';
import { SessionStorageRef } from '../../../core/services/browser/session-storage-ref.service';
import { AdminAuthService } from '../admin-auth.service';
import { UserInfo } from 'node:os';

@Component({
  selector: 'app-deprecated-management',
  imports: [
    AsyncPipe,
    CustomSortCardComponent,
    FormsModule,
    TranslateModule,
    DatePipe,
    NgOptimizedImage
  ],
  templateUrl: './deprecated-management.component.html',
  styleUrl: './deprecated-management.component.scss'
})
export class DeprecatedManagementComponent implements OnInit {
  productService = inject(ProductService);
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  themeService = inject(ThemeService);
  adminAuthService = inject(AdminAuthService);

  // Deprecate form dialog state
  showDeprecatedProductDialog = false;
  isClosing = false;
  isDeprecating = false;

  // Success dialog state
  showSuccessDialog = false;
  isClosingSuccessDialog = false;
  successPullRequestUrl: string | null = null;
  isCopySuccessVisible = false;
  successMode: 'deprecate' | 'undeprecate' | null = null;
  private successDialogCloseTimer?: ReturnType<typeof setTimeout>;

  // Undeprecate confirm dialog state
  showUndeprecateConfirmDialog = false;
  isClosingUndeprecateDialog = false;
  isUndeprecating = false;
  undeprecateProductId = '';

  dropdownOpen = false;
  deprecatedRequest: DeprecatedRequest = {
    productId: '',
    successorUrl: '',
    addReadme: false,
    deprecated: false,
    pullRequestAction: PullRequestAction.ADD,
    deprecationRequester: ''
  };
  selectableProductIds: string[] = [];
  filteredProductIds: string[] = [];
  deprecatedRows: DeprecatedProductInfo[] = [];
  filteredDeprecatedRows: DeprecatedProductInfo[] = [];
  tableSearchTerm = '';
  deprecatedResponse: DeprecatedResponse = {
    productDeprecations: [],
    pullRequestUrl: null
  };
  moderatorName!: string | undefined;
  token: string | undefined = '';
  // Validation state
  validationErrors: { productId?: string; successorUrl?: string } = {};

  constructor(private readonly storageRef: SessionStorageRef) {}

  async ngOnInit(): Promise<void> {
    const userInfo = this.adminAuthService.loadFromSessionStorage();
    this.moderatorName = userInfo?.username;
    this.token = userInfo?.token;
    this.deprecatedRequest.deprecationRequester = this.moderatorName;
    await this.refreshDeprecatedRows();
  }

  trigger() {
    this.showDeprecatedProductDialog = true;
    this.isCopySuccessVisible = false;
    this.deprecatedResponse.pullRequestUrl = null;
  }

  closeDialog() {
    this.isClosing = true;
    setTimeout(() => {
      this.showDeprecatedProductDialog = false;
      this.isClosing = false;
      this.isDeprecating = false;
      this.isCopySuccessVisible = false;
      this.deprecatedRequest = {
        productId: '',
        successorUrl: '',
        addReadme: false,
        deprecated: false
      };
      this.validationErrors = {};
    }, 250);
  }

  async openExtensionDropdown() {
    const selectableProducts = await this.loadAllProductIds(null);
    this.selectableProductIds = selectableProducts.map(product => product.id);
    this.filterProducts(this.deprecatedRequest.productId);
    this.dropdownOpen = true;
  }

  async deprecatedProduct() {
    if (this.isDeprecating) {
      return;
    }

    // Validate inputs
    if (!this.validateForm()) {
      return;
    }
    this.isDeprecating = true;
    this.dropdownOpen = false;

    try {
      this.deprecatedResponse = await firstValueFrom(
        this.productService.updateDeprecatedProduct(
          this.deprecatedRequest,
          this.token
        )
      );
      await this.applyRowsFromUpdateResponse(this.deprecatedResponse);
      this.validationErrors = {};
      this.successPullRequestUrl =
        this.deprecatedResponse.pullRequestUrl ?? null;
      // Close form dialog immediately and show success dialog
      this.showDeprecatedProductDialog = false;
      this.isClosing = false;
      this.successMode = 'deprecate';
      this.showSuccessDialog = true;
      this.isCopySuccessVisible = false;
    } finally {
      this.isDeprecating = false;
    }
  }

  closeSuccessDialog(): void {
    if (this.isClosingSuccessDialog) {
      return;
    }

    const shouldResetDeprecateForm = this.successMode === 'deprecate';

    this.isClosingSuccessDialog = true;

    if (this.successDialogCloseTimer) {
      clearTimeout(this.successDialogCloseTimer);
    }

    this.successDialogCloseTimer = setTimeout(() => {
      this.showSuccessDialog = false;
      this.isClosingSuccessDialog = false;
      this.successPullRequestUrl = null;
      this.isCopySuccessVisible = false;
      this.successMode = null;

      if (shouldResetDeprecateForm) {
        // Reset deprecate form after closing deprecate success dialog
        this.deprecatedRequest = {
          productId: '',
          successorUrl: '',
          addReadme: false,
          deprecated: false,
          pullRequestAction: PullRequestAction.ADD,
          deprecationRequester: this.moderatorName
        };
      }
    }, 250);
  }

  async copyPullRequestUrl(): Promise<void> {
    if (!this.hasPullRequestUrl() || !navigator?.clipboard) {
      return;
    }

    const url = this.successPullRequestUrl?.trim();
    if (!url) {
      return;
    }

    await navigator.clipboard.writeText(url);
    this.isCopySuccessVisible = true;
    setTimeout(() => {
      this.isCopySuccessVisible = false;
    }, 1500);
  }

  hasPullRequestUrl(): boolean {
    const url = this.successPullRequestUrl?.trim();
    return !!url && url.toLowerCase() !== 'null';
  }

  onClickCheckBoxReadme(): void {
    this.deprecatedRequest.addReadme = true;
  }

  validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;

    // Validate productId (required)
    if (
      !this.deprecatedRequest.productId ||
      this.deprecatedRequest.productId.trim() === ''
    ) {
      this.validationErrors['productId'] = 'Extension ID is required';
      isValid = false;
    }

    // Validate successorUrl (optional but must match pattern if provided)
    if (
      this.deprecatedRequest.successorUrl &&
      this.deprecatedRequest.successorUrl.trim() !== ''
    ) {
      const urlPattern = /^(http|https):\/\/.*$/;
      if (!urlPattern.test(this.deprecatedRequest.successorUrl)) {
        this.validationErrors['successorUrl'] =
          'URL must start with http:// or https://';
        isValid = false;
      }
    }

    return isValid;
  }

  selectExtension(productId: string) {
    this.deprecatedRequest.productId = productId;
    this.deprecatedRequest.deprecated = true;
    this.dropdownOpen = false;
    this.deprecatedRequest.pullRequestAction = PullRequestAction.ADD;
  }

  async confirmUndeprecate(productId: string): Promise<void> {
    this.undeprecateProductId = productId;
    this.showUndeprecateConfirmDialog = true;
  }

  closeUndeprecateDialog(): void {
    if (this.isUndeprecating) {
      return;
    }
    this.isClosingUndeprecateDialog = true;
    setTimeout(() => {
      this.showUndeprecateConfirmDialog = false;
      this.isClosingUndeprecateDialog = false;
      this.undeprecateProductId = '';
    }, 250);
  }

  async executeUndeprecate(): Promise<void> {
    if (this.isUndeprecating) {
      return;
    }
    this.isUndeprecating = true;

    try {
      const request: DeprecatedRequest = {
        productId: this.undeprecateProductId,
        successorUrl: '',
        addReadme: true,
        deprecated: null,
        deprecationRequester: this.moderatorName,
        pullRequestAction: PullRequestAction.REMOVE
      };
      this.deprecatedResponse = await firstValueFrom(
        this.productService.updateDeprecatedProduct(request, this.token)
      );
      await this.applyRowsFromUpdateResponse(this.deprecatedResponse);

      // Close confirm dialog and show success dialog
      this.showUndeprecateConfirmDialog = false;
      this.isClosingUndeprecateDialog = false;
      this.undeprecateProductId = '';

      this.successPullRequestUrl =
        this.deprecatedResponse.pullRequestUrl ?? null;
      this.successMode = 'undeprecate';
      this.showSuccessDialog = true;
      this.isCopySuccessVisible = false;
    } finally {
      this.isUndeprecating = false;
    }
  }

  getDeprecatedTime(row: DeprecatedProductInfo): string {
    return row.deprecationDate?.trim() || '-';
  }

  getRequester(row: DeprecatedProductInfo): string {
    return row.deprecationRequester?.trim() || '-';
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

  filterTable(searchTerm: string): void {
    this.tableSearchTerm = searchTerm;
    const normalized = (searchTerm || '').trim().toLowerCase();
    if (!normalized) {
      this.filteredDeprecatedRows = [...this.deprecatedRows];
      return;
    }

    this.filteredDeprecatedRows = this.deprecatedRows.filter(row =>
      row.id.toLowerCase().includes(normalized)
    );
  }

  private async loadAllProductIds(
    predicated: boolean | null
  ): Promise<DeprecatedProductInfo[]> {
    return await firstValueFrom(
      this.productService.fetchAllProductIdsByDeprecated(predicated)
    );
  }

  private async refreshDeprecatedRows(): Promise<void> {
    this.deprecatedRows = await this.loadAllProductIds(true);
    this.filterTable(this.tableSearchTerm);
  }

  private async applyRowsFromUpdateResponse(
    response: DeprecatedResponse
  ): Promise<void> {
    if (response?.productDeprecations) {
      this.deprecatedRows = response.productDeprecations;
      this.filterTable(this.tableSearchTerm);
      return;
    }
    await this.refreshDeprecatedRows();
  }
}
