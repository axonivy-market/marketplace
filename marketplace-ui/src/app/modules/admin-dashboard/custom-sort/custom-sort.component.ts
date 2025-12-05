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
import { PageTitleService } from '../../../shared/services/page-title.service';

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
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  private readonly productService = inject(ProductService);
  private readonly adminDashboardService = inject(AdminDashboardService);
  private readonly renderer = inject(Renderer2);
  private readonly document = inject(DOCUMENT);

  sortingExtensions: string[] = [];
  allExtensions: string[] = [];
    isLoading = false;

  productId = '';
  searchTerm = '';
  isSaving = false;
  sortSuccessMessage = '';
  sortErrorMessage = '';

  private readonly PAGE_SIZE = 200;

  ngOnInit(): void {
    this.loadAllProductIds();
    this.pageTitleService.setTitleOnLangChange('common.admin.customSort.pageTitle');
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
    this.isSaving = true;

    this.adminDashboardService
      .sortMarketExtensions(this.sortingExtensions, 'alphabetically')
      .pipe(finalize(() => {
        this.isSaving = false;
      }))
      .subscribe({
        next: () => {
          this.sortSuccessMessage = this.translateService.instant('common.admin.customSort.sortSuccessMessage');
        },
        error: () => {
          this.sortErrorMessage = this.translateService.instant('common.admin.customSort.sortErrorMessage');
        }
      });
  }

  private async loadAllProductIds(): Promise<void> {
    this.isLoading = true;
    try {
      const ids = await this.productService.fetchAllProductIds(this.PAGE_SIZE);
      this.allExtensions = ids;
    } catch {
      this.allExtensions = [];
    } finally {
      this.isLoading = false;
    }
  }
}
