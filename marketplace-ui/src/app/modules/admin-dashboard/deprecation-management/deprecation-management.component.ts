import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { HttpToastService } from '../../../core/services/browser/http-toast.service';
import { CustomSortCardComponent } from '../custom-sort/custom-sort-card/custom-sort-card.component';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { firstValueFrom } from 'rxjs';
import { ProductService } from '../../product/product.service';
import { DeprecationRequest } from '../../../shared/models/deprecation-request';
import { PullRequestAction } from '../../../shared/enums/pullrequest-action';
import { DeprecatedProductInfo } from '../../../shared/models/deprecated-product-info';
import { AdminAuthService } from '../admin-auth.service';
import { DeprecationMode } from '../../../shared/enums/deprecation-mode.enum';
import { ArchiveAction } from '../../../shared/enums/archive-action.enum';

import { DeprecationFormDialogComponent } from './dialogs/deprecation-form-dialog/deprecation-form-dialog.component';
import { DeprecationResultDialogComponent } from './dialogs/deprecation-result-dialog/deprecation-result-dialog.component';
import { RemoveDeprecatedConfirmDialogComponent } from './dialogs/remove-deprecated-confirm-dialog/remove-deprecated-confirm-dialog.component';

@Component({
  selector: 'app-deprecated-management',
  imports: [
    CustomSortCardComponent,
    FormsModule,
    TranslateModule,
    DatePipe,
    DeprecationFormDialogComponent,
    DeprecationResultDialogComponent,
    RemoveDeprecatedConfirmDialogComponent
  ],
  templateUrl: './deprecation-management.component.html',
  styleUrl: './deprecation-management.component.scss'
})
export class DeprecationManagementComponent implements OnInit {
  private readonly DIALOG_CLOSE_DELAY_MS = 250;
  private readonly COPY_SUCCESS_VISIBLE_DURATION_MS = 1500;

  productService = inject(ProductService);
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  adminAuthService = inject(AdminAuthService);
  translateService = inject(TranslateService);
  httpToastService = inject(HttpToastService);

  // Deprecate form dialog state
  showDeprecatedProductDialog = false;
  isClosing = false;
  isDeprecating = false;

  // Success dialog state
  showSuccessDialog = false;
  isClosingSuccessDialog = false;
  successPullRequestUrl: string | null = null;
  isCopySuccessVisible = false;
  successMode: DeprecationMode | null = null;
  private successDialogCloseTimer?: ReturnType<typeof setTimeout>;

  showRemoveDeprecationConfirmDialog = false;
  isClosingRemoveDeprecationDialog = false;
  isRemoving = false;
  productId = '';

  // Archive confirm dialog state
  showArchiveConfirmDialog = false;
  isClosingArchiveDialog = false;
  isArchiving = false;
  archiveTargetRow: DeprecatedProductInfo | null = null;

  dropdownOpen = false;
  deprecationRequest: DeprecationRequest = this.createEmptyDeprecationRequest();
  selectableProductIds: string[] = [];
  filteredProductIds: string[] = [];
  deprecatedItems: DeprecatedProductInfo[] = [];
  filteredDeprecatedRows: DeprecatedProductInfo[] = [];
  tableSearchTerm = '';
  moderatorName = '';
  // Validation state
  validationErrors: {
    productId?: string;
    alternativeExtension?: string;
    successorUrl?: string;
  } = {};

  ngOnInit(): void {
    const userInfo = this.adminAuthService.loadFromSessionStorage();
    this.moderatorName = userInfo?.username?.trim() || '';
    this.deprecationRequest.deprecationRequester = this.moderatorName;
    this.initializeDeprecatedRows();
  }

  private createEmptyDeprecationRequest(deprecationDate: Date | null = new Date(), deprecationRequester = ''):
    DeprecationRequest {
    return {
      hasAlternativeExtension: false,
      alternativeExtension: '',
      successorUrl: '',
      isAddReadme: false,
      isDeprecated: false,
      pullRequestAction: PullRequestAction.ADD,
      deprecationRequester,
      deprecationDate
    };
  }

  private initializeDeprecatedRows(): void {
    this.refreshDeprecatedRows();
  }

  openDeprecationDialog() {
    this.showDeprecatedProductDialog = true;
    this.isCopySuccessVisible = false;
    this.successPullRequestUrl = null;
  }

  closeDialog() {
    this.isClosing = true;
    setTimeout(() => {
      this.showDeprecatedProductDialog = false;
      this.isClosing = false;
      this.isDeprecating = false;
      this.isCopySuccessVisible = false;
      this.productId = '';
      this.deprecationRequest = this.createEmptyDeprecationRequest(null, this.moderatorName);
      this.validationErrors = {};
    }, this.DIALOG_CLOSE_DELAY_MS);
  }

  async openExtensionDropdown() {
    const allProducts = await this.loadAllProductIds();
    const alreadyDeprecatedIds = new Set(this.deprecatedItems.map(item => item.id));
    this.selectableProductIds = allProducts.map(product => product.id).filter(id => !alreadyDeprecatedIds.has(id));
    this.filterProducts(this.productId);
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
    this.deprecationRequest.deprecationRequester = this.moderatorName;
    this.deprecationRequest.deprecationDate = new Date();
    try {
      this.successPullRequestUrl = await firstValueFrom(
        this.productService.updateDeprecatedProduct(this.productId, this.deprecationRequest)
      );
      await this.applyRowsFromUpdateResponse(this.buildDeprecatedItem(), PullRequestAction.ADD);
      this.validationErrors = {};
      this.showDeprecatedProductDialog = false;
      this.isClosing = false;
      this.successMode = DeprecationMode.DEPRECATE;
      this.showSuccessDialog = true;
      this.isCopySuccessVisible = false;
    } finally {
      this.isDeprecating = false;
    }
  }

  private buildDeprecatedItem(): DeprecatedProductInfo {
    return {
      id: this.productId,
      deprecationDate: this.deprecationRequest?.deprecationDate?.toISOString(),
      deprecationRequester: this.moderatorName
    };
  }

  closeSuccessDialog(): void {
    if (this.isClosingSuccessDialog) {
      return;
    }

    const shouldResetDeprecateForm = this.successMode === DeprecationMode.DEPRECATE;

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
        this.productId = '';
        this.deprecationRequest = this.createEmptyDeprecationRequest(null, this.moderatorName);
      }
    }, this.DIALOG_CLOSE_DELAY_MS);
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
    }, this.COPY_SUCCESS_VISIBLE_DURATION_MS);
  }

  hasPullRequestUrl(): boolean {
    const url = this.successPullRequestUrl?.trim();
    return !!url && url.toLowerCase() !== 'null';
  }

  onClickCheckBoxReadme(): void {
    this.deprecationRequest.isAddReadme = true;
  }

  validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;

    // Validate productId (required)
    if (!this.productId || this.productId.trim() === '') {
      this.validationErrors.productId = this.translateService.instant(
        'common.admin.deprecation.validation.extensionIdRequired'
      );
      isValid = false;
    }

    if (this.deprecationRequest.hasAlternativeExtension) {
      if (!this.deprecationRequest.alternativeExtension?.trim()) {
        this.validationErrors.alternativeExtension = this.translateService.instant(
          'common.admin.deprecation.validation.alternativeExtensionRequired'
        );
        isValid = false;
      }

      if (this.deprecationRequest.successorUrl?.trim()) {
        const urlPattern = /^(http|https):\/\/.*$/;
        if (!urlPattern.test(this.deprecationRequest.successorUrl)) {
          this.validationErrors.successorUrl = this.translateService.instant(
            'common.admin.deprecation.validation.invalidSuccessorUrl'
          );
          isValid = false;
        }
      } else {
        this.validationErrors.successorUrl = this.translateService.instant(
          'common.admin.deprecation.validation.successorRequired'
        );
        isValid = false;
      }
    } else {
      this.deprecationRequest.alternativeExtension = '';
      this.deprecationRequest.successorUrl = '';
    }

    return isValid;
  }

  selectExtension(productId: string) {
    this.productId = productId;
    this.deprecationRequest.isDeprecated = true;
    this.dropdownOpen = false;
    this.deprecationRequest.pullRequestAction = PullRequestAction.ADD;
  }

  async confirmRemovedDeprecation(productId: string): Promise<void> {
    this.productId = productId;
    this.showRemoveDeprecationConfirmDialog = true;
  }

  async toggleArchiveStatus(row: DeprecatedProductInfo): Promise<void> {
    this.archiveTargetRow = row;
    this.showArchiveConfirmDialog = true;
  }

  closeArchiveConfirmDialog(): void {
    if (this.isArchiving) {
      return;
    }
    this.isClosingArchiveDialog = true;
    setTimeout(() => {
      this.showArchiveConfirmDialog = false;
      this.isClosingArchiveDialog = false;
      this.archiveTargetRow = null;
    }, this.DIALOG_CLOSE_DELAY_MS);
  }

  async executeToggleArchive(): Promise<void> {
    if (this.isArchiving || !this.archiveTargetRow) {
      return;
    }
    this.isArchiving = true;

    const row = this.archiveTargetRow;
    const action = row.isArchived ? ArchiveAction.UNARCHIVE : ArchiveAction.ARCHIVE;

    try {
      await firstValueFrom(
        this.productService.updateArchiveStatus(row.id, action)
      );
      row.isArchived = !row.isArchived;
    } catch (error) {
      // Parse error response and show toast notification
      this.showArchiveErrorToast(error);
      this.showArchiveConfirmDialog = false;
      this.isClosingArchiveDialog = false;
      this.archiveTargetRow = null;
    } finally {
      this.isArchiving = false;
    }
  }

  private showArchiveErrorToast(error: unknown): void {
    let messageKey = 'common.error.description.default';
    if (error instanceof HttpErrorResponse) {
      try {
        const errorBody = typeof error.error === 'string' ? JSON.parse(error.error) : error.error;
        messageKey = errorBody?.messageDetails || messageKey;
      } catch {
        messageKey = error.error || messageKey;
      }
    }
    this.httpToastService.publishError({
      status: error instanceof HttpErrorResponse ? error.status : 0,
      messageKey,
      timestamp: Date.now()
    });
  }

  closeRemoveDeprecationDialog(): void {
    if (this.isRemoving) {
      return;
    }
    this.isClosingRemoveDeprecationDialog = true;
    setTimeout(() => {
      this.showRemoveDeprecationConfirmDialog = false;
      this.isClosingRemoveDeprecationDialog = false;
      this.productId = '';
    }, this.DIALOG_CLOSE_DELAY_MS);
  }

  async executeRemoveDeprecation(): Promise<void> {
    if (this.isRemoving) {
      return;
    }
    this.isRemoving = true;

    try {
      const request: DeprecationRequest = {
        successorUrl: '',
        isAddReadme: true,
        isDeprecated: null,
        deprecationRequester: this.moderatorName,
        deprecationDate: new Date(),
        pullRequestAction: PullRequestAction.REMOVE
      };
      this.successPullRequestUrl = await firstValueFrom(
        this.productService.updateDeprecatedProduct(this.productId, request)
      );
      await this.applyRowsFromUpdateResponse(this.buildDeprecatedItem(), PullRequestAction.REMOVE);

      // Close confirm dialog and show success dialog
      this.showRemoveDeprecationConfirmDialog = false;
      this.isClosingRemoveDeprecationDialog = false;
      this.productId = '';
      this.successMode = DeprecationMode.UNDEPRECATE;
      this.showSuccessDialog = true;
      this.isCopySuccessVisible = false;
    } finally {
      this.isRemoving = false;
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
      this.filteredDeprecatedRows = [...this.deprecatedItems];
      return;
    }

    this.filteredDeprecatedRows = this.deprecatedItems.filter(row => row.id.toLowerCase().includes(normalized));
  }

  private loadAllProductIds(isDeprecated: boolean | undefined = undefined): Promise<DeprecatedProductInfo[]> {
    return firstValueFrom(this.productService.fetchAllProductIdsByDeprecated(isDeprecated));
  }

  private async refreshDeprecatedRows(): Promise<void> {
    this.deprecatedItems = await this.loadAllProductIds(true);
    this.filterTable(this.tableSearchTerm);
  }

  private async applyRowsFromUpdateResponse(
    deprecatedProductInfo: DeprecatedProductInfo,
    action: PullRequestAction
  ): Promise<void> {
    if (deprecatedProductInfo) {
      if (action === PullRequestAction.ADD) {
        this.deprecatedItems.push(deprecatedProductInfo);
      } else {
        this.deprecatedItems = this.deprecatedItems.filter(item => item.id !== deprecatedProductInfo.id);
      }
      this.filterTable(this.tableSearchTerm);
      return;
    }
    await this.refreshDeprecatedRows();
  }
}
