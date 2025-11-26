import { CommonModule, DOCUMENT } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, Renderer2, ViewEncapsulation } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { CdkDragDrop, CdkDragEnd, CdkDragEnter, CdkDragStart, DragDropModule, transferArrayItem } from '@angular/cdk/drag-drop';
import { ProductService } from '../../product/product.service';
import { AdminDashboardService } from '../admin-dashboard.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-custom-sort',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    DragDropModule
],
  templateUrl: './custom-sort.component.html',
  styleUrls: ['./custom-sort.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})

export class CustomSortComponent implements OnInit {
  token = '';
  errorMessage = '';
  isAuthenticated = false;
  isLoading = false;

  productId = '';
  marketItemPath = '';
  overrideMarketItemPath = false;
  showSyncJob = true;

  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  private readonly productService = inject(ProductService);
  private readonly adminDashboardService = inject(AdminDashboardService);
  private readonly renderer = inject(Renderer2);
  private readonly document = inject(DOCUMENT);

  sortingExtensions: string[] = [];
  allExtensions: string[] = [];
  searchTerm = '';
  isSaving = false;
  sortSuccessMessage = '';
  sortErrorMessage = '';

  private readonly PAGE_SIZE = 200;

  ngOnInit(): void {
    void this.loadAllProductIds();
  }

  get filteredAvailableExtensions(): string[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      return this.allExtensions;
    }
    return this.allExtensions.filter((id) => id.toLowerCase().includes(term));
  }

  clearSearch(): void {
    this.searchTerm = '';
  }

  drop(event: CdkDragDrop<string[]>) {
    if (event.previousContainer === event.container) {
      // same list, you can reorder if you like:
      // moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      // moved from one list to another
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    }
  }

  setDragPreviewWidth(event: CdkDragStart<string>): void {
    this.applyPreviewWidth(event.source.element.nativeElement.getBoundingClientRect().width);
  }

  resetDragPreviewWidth(_: CdkDragEnd<string>): void {
    const root = this.document?.documentElement;
    if (root) {
      this.renderer.removeStyle(root, '--drag-preview-width');
    }
  }

  adjustPreviewWidthOnEnter(event: CdkDragEnter<string[]>): void {
    this.applyPreviewWidth(event.container.element.nativeElement.getBoundingClientRect().width);
  }

  private applyPreviewWidth(width: number): void {
    const root = this.document?.documentElement;
    if (!root || width <= 0) {
      return;
    }
    this.renderer.setStyle(root, '--drag-preview-width', `${width}px`);
  }

  sortMarketExtensions(): void {
    this.sortSuccessMessage = '';
    this.sortErrorMessage = '';

    if (this.sortingExtensions.length === 0) {
      this.sortErrorMessage = 'Please drag at least one extension into the sorted list before saving.';
      return;
    }

    this.isSaving = true;

    this.adminDashboardService
      .sortMarketExtensions(this.sortingExtensions, 'alphabetically')
      .pipe(finalize(() => {
        this.isSaving = false;
      }))
      .subscribe({
        next: () => {
          this.sortSuccessMessage = 'Sorting saved successfully.';
        },
        error: (error) => {
          console.error('Failed to persist custom sorting', error);
          this.sortErrorMessage = 'Saving the sorting failed. Please try again.';
        }
      });
  }

  private async loadAllProductIds(): Promise<void> {
    this.isLoading = true;
    try {
      const ids = await this.productService.fetchAllProductIds(this.PAGE_SIZE);
      this.allExtensions = ids;
    } catch (error) {
      console.warn('Failed to load product ids', error);
      this.allExtensions = [];
    } finally {
      this.isLoading = false;
    }
  }
}
