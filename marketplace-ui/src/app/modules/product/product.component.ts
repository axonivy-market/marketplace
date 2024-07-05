import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  WritableSignal,
  inject,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { ThemeService } from '../../core/services/theme/theme.service';
import { TypeOption } from '../../shared/enums/type-option.enum';
import { SortOption } from '../../shared/enums/sort-option.enum';
import { Criteria } from '../../shared/models/criteria.model';
import { Product } from '../../shared/models/product.model';
import { ProductCardComponent } from './product-card/product-card.component';
import { ProductFilterComponent } from './product-filter/product-filter.component';
import { ProductService } from './product.service';
import { ProductApiResponse } from '../../shared/models/apis/product-response.model';
import { Link } from '../../shared/models/apis/link.model';
import { Page } from '../../shared/models/apis/page.model';
import { Language } from '../../shared/enums/language.enum';
import { LanguageService } from '../../core/services/language/language.service';

const SEARCH_DEBOUNCE_TIME = 500;

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    ProductCardComponent,
    ProductFilterComponent
  ],
  providers: [ProductService],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent implements AfterViewInit, OnDestroy {
  products: WritableSignal<Product[]> = signal([]);
  subscriptions: Subscription[] = [];
  searchTextChanged = new Subject<string>();
  criteria: Criteria = {
    search: '',
    type: TypeOption.All_TYPES,
    sort: SortOption.POPULARITY,
    language: Language.EN
  };
  responseLink!: Link;
  responsePage!: Page;

  productService = inject(ProductService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);

  router = inject(Router);
  @ViewChild('observer', { static: true }) observerElement!: ElementRef;

  constructor() {
    this.loadProductItems();
    this.subscriptions.push(
      this.searchTextChanged
        .pipe(debounceTime(SEARCH_DEBOUNCE_TIME))
        .subscribe(value => {
          this.criteria = {
            ...this.criteria,
            search: value
          };
          this.loadProductItems(true);
        })
    );
  }

  ngAfterViewInit(): void {
    this.setupIntersectionObserver();
  }

  viewProductDetail(productId: string) {
    this.router.navigate(['', productId]);
  }

  onFilterChange(selectedType: TypeOption) {
    this.criteria = {
      ...this.criteria,
      nextPageHref: '',
      type: selectedType
    };
    this.loadProductItems(true);
  }

  onSortChange(selectedSort: SortOption) {
    this.criteria = {
      ...this.criteria,
      nextPageHref: '',
      sort: selectedSort
    };
    this.loadProductItems(true);
  }

  onSearchChanged(searchString: string) {
    this.searchTextChanged.next(searchString);
  }

  loadProductItems(shouldCleanData = false) {
    this.criteria.language = this.languageService.getSelectedLanguage();
    this.subscriptions.push(
      this.productService.findProductsByCriteria(this.criteria).subscribe((response: ProductApiResponse) => {
        const newProducts = response._embedded.products;
        if (shouldCleanData) {
          this.products.set(newProducts);
        } else {
          this.products.update(existingProducts => existingProducts.concat(newProducts));
        }
        this.responseLink = response._links;
        this.responsePage = response.page;
      })
    );
  }

  setupIntersectionObserver() {
    const options = { root: null, rootMargin: '0px', threshold: 0.1 };
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting && this.hasMore()) {
          this.criteria.nextPageHref = this.responseLink?.next?.href;
          this.loadProductItems();
        }
      });
    }, options);

    observer.observe(this.observerElement.nativeElement);
  }

  hasMore() {
    if (!this.responsePage || !this.responseLink) {
      return false;
    }
    return this.responsePage.number < this.responsePage.totalPages
      && this.responseLink?.next !== undefined;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => {
      sub.unsubscribe();
    });
  }
}
