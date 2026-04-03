import { Component, inject, OnInit } from '@angular/core';
import { AsyncPipe, DatePipe } from '@angular/common';

import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { CustomSortCardComponent } from '../custom-sort/custom-sort-card/custom-sort-card.component';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { catchError, firstValueFrom, Observable, of, tap } from 'rxjs';
import { ProductService } from '../../product/product.service';
import { DeprecatedRequest } from '../../../shared/models/deprecated-request';
import { PullRequestAction } from '../../../shared/enums/pullrequest-action';
import { DeprecatedResponse } from '../../../shared/models/deprecated-response';
import { DeprecatedProductInfo } from '../../../shared/models/deprecated-product-info';
import { HttpErrorResponse } from '@angular/common/http';
import { ADMIN_SESSION_TOKEN, UNAUTHORIZED } from '../../../shared/constants/common.constant';
import { AuthService, UserInfo } from '../../../auth/auth.service';
import { SessionStorageRef } from '../../../core/services/browser/session-storage-ref.service';
import { AdminAuthService } from '../admin-auth.service';

@Component({
  selector: 'app-deprecated-management',
  imports: [
    AsyncPipe,
    CustomSortCardComponent,
    FormsModule,
    TranslateModule,
    DatePipe
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

  // Undeprecate confirm dialog state
  showUndeprecateConfirmDialog = false;
  isClosingUndeprecateDialog = false;
  showDeprecatedProductDialog = false;
  isClosing = false;
  isDeprecating = false;
  isCopySuccessVisible = false;
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
  deprecatedResponse: DeprecatedResponse = {
    productDeprecations: [],
    pullRequestUrl: null
  };
  moderatorName!: string | undefined;
  // Validation state
  validationErrors: { productId?: string; successorUrl?: string } = {};

  constructor(private readonly storageRef: SessionStorageRef) {}

  async ngOnInit(): Promise<void> {
    this.moderatorName =
      this.adminAuthService.loadFromSessionStorage()?.username;
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
        this.productService.updateDeprecatedProduct(this.deprecatedRequest)
      );
      await this.applyRowsFromUpdateResponse(this.deprecatedResponse);
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
      addReadme: true,
      deprecated: false,
      deprecationRequester: this.moderatorName,
      pullRequestAction: PullRequestAction.REMOVE
    };
    this.deprecatedResponse = await firstValueFrom(
      this.productService.updateDeprecatedProduct(request)
    );
    await this.applyRowsFromUpdateResponse(this.deprecatedResponse);
    this.closeUndeprecateDialog();
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

  private async loadAllProductIds(
    predicated: boolean | null
  ): Promise<DeprecatedProductInfo[]> {
    return await firstValueFrom(
      this.productService.fetchAllProductIdsByDeprecated(predicated)
    );
  }

  private async refreshDeprecatedRows(): Promise<void> {
    this.deprecatedRows = await this.loadAllProductIds(true);
  }

  private async applyRowsFromUpdateResponse(
    response: DeprecatedResponse
  ): Promise<void> {
    if (response?.productDeprecations) {
      this.deprecatedRows = response.productDeprecations;
      return;
    }
    await this.refreshDeprecatedRows();
  }
}
