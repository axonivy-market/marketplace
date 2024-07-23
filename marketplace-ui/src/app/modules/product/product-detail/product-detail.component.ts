import {
  Component,
  ElementRef,
  HostListener,
  WritableSignal,
  inject,
  signal
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../product.service';
import { TranslateModule } from '@ngx-translate/core';
import { MarkdownModule, MarkdownService } from 'ngx-markdown';
import { ProductDetail } from '../../../shared/models/product-detail.model';
import { ProductModuleContent } from '../../../shared/models/product-module-content.model';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { CommonModule } from '@angular/common';
import { ProductDetailInformationTabComponent } from './product-detail-information-tab/product-detail-information-tab.component';
import { ProductDetailVersionActionComponent } from './product-detail-version-action/product-detail-version-action.component';
import { ProductDetailMavenContentComponent } from './product-detail-maven-content/product-detail-maven-content.component';
import { PRODUCT_DETAIL_TABS } from '../../../shared/constants/common.constant';
import { NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { LanguageService } from '../../../core/services/language/language.service';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { ProductDetailService } from './product-detail.service';
import { ProductDetailFeedbackComponent } from './product-detail-feedback/product-detail-feedback.component';
import { ProductFeedbackService } from './product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { AuthService } from '../../../auth/auth.service';
import { ProductStarRatingNumberComponent } from './product-star-rating-number/product-star-rating-number.component';
import { ProductInstallationCountActionComponent } from './product-installation-count-action/product-installation-count-action.component';
import { ProductTypeIconPipe } from '../../../shared/pipes/icon.pipe';
import { ProductStarRatingService } from './product-detail-feedback/product-star-rating-panel/product-star-rating.service';

export interface DetailTab {
  activeClass: string;
  tabId: string;
  value: string;
  label: string;
}

const STORAGE_ITEM = 'activeTab';
const DEFAULT_ACTIVE_TAB = 'description';
@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    ProductDetailVersionActionComponent,
    CommonModule,
    ProductStarRatingNumberComponent,
    TranslateModule,
    MarkdownModule,
    ProductDetailInformationTabComponent,
    ProductDetailMavenContentComponent,
    NgbNavModule,
    MultilingualismPipe,
    ProductDetailFeedbackComponent,
    ProductInstallationCountActionComponent,
    ProductTypeIconPipe
  ],
  providers: [ProductService, MarkdownService],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent {
  themeService = inject(ThemeService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  productService = inject(ProductService);
  languageService = inject(LanguageService);
  productDetailService = inject(ProductDetailService);
  productFeedbackService = inject(ProductFeedbackService);
  productStarRatingService = inject(ProductStarRatingService);
  appModalService = inject(AppModalService);
  authService = inject(AuthService);
  elementRef = inject(ElementRef);

  resizeObserver: ResizeObserver;

  productDetail: WritableSignal<ProductDetail> = signal({} as ProductDetail);
  productModuleContent: WritableSignal<ProductModuleContent> = signal(
    {} as ProductModuleContent
  );
  detailContent!: DetailTab;
  detailTabs = PRODUCT_DETAIL_TABS;
  activeTab = DEFAULT_ACTIVE_TAB;
  isDropdownOpen: WritableSignal<boolean> = signal(false);
  isTabDropdownShown: WritableSignal<boolean> = signal(false);
  selectedVersion = '';
  showPopup!: boolean;
  isMobileMode = signal<boolean>(false);
  installationCount = 0;

  @HostListener('window:popstate', ['$event'])
  onPopState() {
    this.activeTab = window.location.hash.split('#tab-')[1];
    if (this.activeTab === undefined) {
      this.activeTab = DEFAULT_ACTIVE_TAB;
    }
    this.updateDropdownSelection();
  }

  constructor() {
    this.resizeObserver = new ResizeObserver(() => {
      this.updateDropdownSelection();
    });
  }

  ngOnInit(): void {
    const productId = this.route.snapshot.params['id'];
    this.productDetailService.productId.set(productId);
    if (productId) {
      this.productService
        .getProductDetails(productId)
        .subscribe(productDetail => {
          this.productDetail.set(productDetail);
          this.productModuleContent.set(productDetail.productModuleContent);
          this.productDetailService.productNames.set(productDetail.names);
          localStorage.removeItem(STORAGE_ITEM);
          this.installationCount = productDetail.installationCount;
        });
      this.productFeedbackService.initFeedbacks();
      this.productStarRatingService.fetchData();
    }

    const savedTab = localStorage.getItem(STORAGE_ITEM);
    if (savedTab) {
      this.activeTab = savedTab;
    }
    this.updateDropdownSelection();
  }

  ngAfterViewInit(): void {
    this.checkMediaSize();
    this.productFeedbackService.findProductFeedbackOfUser().subscribe(() => {
      this.route.queryParams.subscribe(params => {
        this.showPopup = params['showPopup'] === 'true';
        if (this.showPopup && this.authService.getToken()) {
          this.appModalService
            .openAddFeedbackDialog()
            .then(() => this.removeQueryParam())
            .catch(() => this.removeQueryParam());
        }
      });
    });
  }

  getContent(value: string): boolean {
    const content = this.productModuleContent();
    const conditions: { [key: string]: boolean } = {
      description: content.description != null,
      demo: content.demo != null && content.demo !== '',
      setup: content.setup != null && content.setup !== '',
      dependency: content.isDependency
    };

    return conditions[value] ?? false;
  }

  loadDetailTabs(selectedVersion: string) {
    const tag =
      selectedVersion.replaceAll('Version ', 'v') ||
      this.productDetail().newestReleaseVersion;
    this.productService
      .getProductDetailsWithVersion(this.productDetail().id, tag)
      .subscribe(updatedProductDetail => {
        this.productModuleContent.set(
          updatedProductDetail.productModuleContent
        );
      });
  }

  onTabChange(event: Event) {
    const selectedTab = (event.target as HTMLSelectElement).value;
    this.setActiveTab(selectedTab);
    this.isTabDropdownShown.update(value => !value);
    this.onTabDropdownShown();
  }

  updateDropdownSelection() {
    const dropdown = document.getElementById(
      'tab-group-dropdown'
    ) as HTMLSelectElement;
    if (dropdown) {
      dropdown.value = this.activeTab;
    }
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    const hash = '#tab-' + tab;
    const path = window.location.pathname;
    if (history.pushState) {
      history.pushState(null, '', path + hash);
    } else {
      window.location.hash = hash;
    }
    this.updateDropdownSelection();

    localStorage.setItem(STORAGE_ITEM, tab);
  }

  onShowInfoContent() {
    this.isDropdownOpen.update(value => !value);
  }

  onTabDropdownShown() {
    this.isTabDropdownShown.set(!this.isTabDropdownShown());
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    if (
      !this.elementRef.nativeElement
        .querySelector('.form-select')
        .contains(event.target) &&
      this.isTabDropdownShown()
    ) {
      this.onTabDropdownShown();
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.checkMediaSize();
  }

  checkMediaSize() {
    const mediaQuery = window.matchMedia('(max-width: 767px)');
    if (mediaQuery.matches) {
      this.isMobileMode.set(true);
    } else {
      this.isMobileMode.set(false);
    }
  }

  onClickRateBtn() {
    const productId = this.productDetailService.productId();
    if (this.authService.getToken()) {
      this.appModalService.openAddFeedbackDialog();
    } else {
      this.authService.redirectToGitHub(productId);
    }
  }

  receiveInstallationCountData(data: number) {
    this.installationCount = data;
  }

  private removeQueryParam(): void {
    this.router.navigate([], {
      queryParams: { showPopup: null },
      queryParamsHandling: 'merge'
    });
  }
}
