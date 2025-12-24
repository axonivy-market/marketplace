import { CommonModule, DOCUMENT } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Component,
  inject,
  OnInit,
  Renderer2,
  ViewEncapsulation
} from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import {
  CdkDragDrop,
  CdkDragEnd,
  CdkDragEnter,
  CdkDragStart,
  DragDropModule,
  moveItemInArray
} from '@angular/cdk/drag-drop';
import { ProductService } from '../../product/product.service';
import { AdminDashboardService } from '../admin-dashboard.service';
import { finalize } from 'rxjs/operators';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { SortOption } from '../../../shared/enums/sort-option.enum';

const SORTED_ID = 'sorted-extensions';
const AVAILABLE_ID = 'available-extensions';
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

  searchTerm = '';
  isLoading = false;
  isSaving = false;

  sortSuccessMessage = '';
  sortErrorMessage = '';

  ngOnInit(): void {
    this.loadAllProductIds();
    this.pageTitleService.setTitleOnLangChange(
      'common.admin.customSort.pageTitle'
    );
  }

  get filteredAvailableExtensions(): string[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      return this.allExtensions;
    }
    return this.allExtensions.filter(id => id.toLowerCase().includes(term));
  }

  clearSearch(): void {
    this.searchTerm = '';
  }

  // Drag and drop extension IDs
  drop(event: CdkDragDrop<string[]>) {
    const item = event.item.data as string;

    if (event.previousContainer === event.container) {
      this.reorderWithinTable(event);
    } else {
      this.transferBetweenTables(event, item);
    }
  }

  private reorderWithinTable(event: CdkDragDrop<string[]>) {
    const tableId = event.container.id;

    if (this.isSorted(tableId)) {
      moveItemInArray(
        this.sortingExtensions,
        event.previousIndex,
        event.currentIndex
      );
      return;
    }

    if (this.isAvailable(tableId)) {
      this.reorderAvailableExtensions(event);
    }
  }

  private reorderAvailableExtensions(event: CdkDragDrop<string[]>) {
    const filtered = this.filteredAvailableExtensions;

    const fromItem = filtered[event.previousIndex];
    const toItem = filtered[event.currentIndex];

    const fromIndex = this.allExtensions.indexOf(fromItem);
    const toIndex = this.allExtensions.indexOf(toItem);

    if (fromIndex > -1 && toIndex > -1) {
      moveItemInArray(this.allExtensions, fromIndex, toIndex);
    }
  }

  private transferBetweenTables(event: CdkDragDrop<string[]>, item: string) {
    const from = event.previousContainer.id;
    const to = event.container.id;

    if (this.isAvailable(from) && this.isSorted(to)) {
      this.moveAvailableToSortedExtensionTable(item, event.currentIndex);
      return;
    }

    if (this.isSorted(from) && this.isAvailable(to)) {
      this.moveSortedToAvailableExtensionTable(item, event.currentIndex);
    }
  }

  private moveAvailableToSortedExtensionTable(item: string, index: number) {
    this.removeFromAvailableExtensionTable(item);
    this.sortingExtensions.splice(index, 0, item);
  }

  private moveSortedToAvailableExtensionTable(item: string, index: number) {
    this.removeFromSortedExtensionTable(item);

    const target = this.filteredAvailableExtensions[index];
    const insertIndex = target
      ? this.allExtensions.indexOf(target)
      : this.allExtensions.length;

    this.allExtensions.splice(insertIndex, 0, item);
  }

  private removeFromAvailableExtensionTable(item: string) {
    const idx = this.allExtensions.indexOf(item);
    if (idx > -1) {
      this.allExtensions.splice(idx, 1);
    }
  }

  private removeFromSortedExtensionTable(item: string) {
    const idx = this.sortingExtensions.indexOf(item);
    if (idx > -1) {
      this.sortingExtensions.splice(idx, 1);
    }
  }

  private isSorted(id: string): boolean {
    return id === SORTED_ID;
  }

  private isAvailable(id: string): boolean {
    return id === AVAILABLE_ID;
  }

  setDragPreviewWidth(event: CdkDragStart<string>): void {
    this.setPreviewWidth(
      event.source.element.nativeElement.getBoundingClientRect().width
    );
  }

  adjustPreviewWidthOnEnter(event: CdkDragEnter<string[]>): void {
    this.setPreviewWidth(
      event.container.element.nativeElement.getBoundingClientRect().width
    );
  }

  resetDragPreviewWidth(_: CdkDragEnd<string>): void {
    this.removePreviewWidth();
  }

  private setPreviewWidth(width: number) {
    if (width <= 0) {
      return;
    }
    this.renderer.setStyle(
      this.document.documentElement,
      '--drag-preview-width',
      `${width}px`
    );
  }

  private removePreviewWidth() {
    this.renderer.removeStyle(
      this.document.documentElement,
      '--drag-preview-width'
    );
  }

  sortMarketExtensions(): void {
    this.sortSuccessMessage = '';
    this.sortErrorMessage = '';
    this.isSaving = true;

    this.adminDashboardService
      .sortMarketExtensions(this.sortingExtensions, SortOption.ALPHABETICALLY)
      .pipe(finalize(() => (this.isSaving = false)))
      .subscribe({
        next: () => {
          this.sortSuccessMessage = this.translateService.instant(
            'common.admin.customSort.sortSuccessMessage'
          );
        },
        error: () => {
          this.sortErrorMessage = this.translateService.instant(
            'common.admin.customSort.sortErrorMessage'
          );
        }
      });
  }

  private async loadAllProductIds(): Promise<void> {
    this.isLoading = true;
    try {
      this.allExtensions =
        await this.productService.fetchAllProductIds();
    } catch {
      this.allExtensions = [];
    } finally {
      this.isLoading = false;
    }
  }
}
