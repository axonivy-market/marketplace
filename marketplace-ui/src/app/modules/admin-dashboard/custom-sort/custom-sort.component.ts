import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { SideMenuComponent } from "../../../shared/components/side-menu/side-menu.component";
import { CdkDragDrop, DragDropModule, transferArrayItem } from '@angular/cdk/drag-drop';
import { ProductService } from '../../product/product.service';

@Component({
  selector: 'app-custom-sort',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    SideMenuComponent,
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
  private readonly productService = inject(ProductService);

  sortingExtensions = ['avs'];
  allExtensions: string[] = [];

  private readonly PAGE_SIZE = 200;

  ngOnInit(): void {
    void this.loadAllProductIds();
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
