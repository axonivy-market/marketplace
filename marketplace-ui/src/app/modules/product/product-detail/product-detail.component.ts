import { CommonModule, NgOptimizedImage } from '@angular/common';
import {
  Component,
  ElementRef,
  HostListener,
  Signal,
  WritableSignal,
  computed,
  inject,
  signal
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { MarkdownModule, MarkdownService } from 'ngx-markdown';
import { Observable } from 'rxjs';
import { AuthService } from '../../../auth/auth.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { CommonDropdownComponent } from '../../../shared/components/common-dropdown/common-dropdown.component';
import {
  PRODUCT_DETAIL_TABS,
  VERSION
} from '../../../shared/constants/common.constant';
import { ItemDropdown } from '../../../shared/models/item-dropdown.model';
import { ProductDetail } from '../../../shared/models/product-detail.model';
import { ProductModuleContent } from '../../../shared/models/product-module-content.model';
import { ProductTypeIconPipe } from '../../../shared/pipes/icon.pipe';
import { MissingReadmeContentPipe } from '../../../shared/pipes/missing-readme-content.pipe';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { ProductTypePipe } from '../../../shared/pipes/product-type.pipe';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { RoutingQueryParamService } from '../../../shared/services/routing.query.param.service';
import { CommonUtils } from '../../../shared/utils/common.utils';
import { ProductService } from '../product.service';
import { ProductDetailFeedbackComponent } from './product-detail-feedback/product-detail-feedback.component';
import { ProductFeedbackService } from './product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { ProductStarRatingService } from './product-detail-feedback/product-star-rating-panel/product-star-rating.service';
import { ProductDetailActionType } from '../../../shared/enums/product-detail-action-type';
import { ProductDetailInformationTabComponent } from './product-detail-information-tab/product-detail-information-tab.component';
import { ProductDetailMavenContentComponent } from './product-detail-maven-content/product-detail-maven-content.component';
import { ProductDetailVersionActionComponent } from './product-detail-version-action/product-detail-version-action.component';
import { ProductDetailService } from './product-detail.service';
import { ProductInstallationCountActionComponent } from './product-installation-count-action/product-installation-count-action.component';
import { ProductStarRatingNumberComponent } from './product-star-rating-number/product-star-rating-number.component';
import { DisplayValue } from '../../../shared/models/display-value.model';

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
    ProductTypePipe,
    ProductDetailFeedbackComponent,
    ProductInstallationCountActionComponent,
    ProductTypeIconPipe,
    MissingReadmeContentPipe,
    CommonDropdownComponent,
    NgOptimizedImage
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
  routingQueryParamService = inject(RoutingQueryParamService);

  resizeObserver: ResizeObserver;

  productDetail: WritableSignal<ProductDetail> = signal({} as ProductDetail);
  productModuleContent: WritableSignal<ProductModuleContent> = signal(
    {} as ProductModuleContent
  );
  productDetailActionType = signal(ProductDetailActionType.STANDARD);
  detailContent!: DetailTab;
  detailTabs = PRODUCT_DETAIL_TABS;
  activeTab = '';
  displayedTabsSignal: Signal<ItemDropdown[]> = computed(() => {
    this.languageService.selectedLanguage();
    return this.getDisplayedTabsSignal();
  });
  isDropdownOpen: WritableSignal<boolean> = signal(false);
  isTabDropdownShown: WritableSignal<boolean> = signal(false);
  selectedVersion = '';
  metaProductJsonUrl: string | undefined = '';
  showPopup!: boolean;
  isMobileMode = signal<boolean>(false);
  installationCount = 0;
  logoUrl ='/assets/images/misc/axonivy-logo-round.png'
  @HostListener('window:popstate', ['$event'])
  onPopState() {
    this.activeTab = window.location.hash.split('#tab-')[1];
    if (this.activeTab === undefined) {
      this.activeTab = DEFAULT_ACTIVE_TAB;
    }
    this.updateDropdownSelection();
  }

  constructor() {
    this.scrollToTop();
    this.resizeObserver = new ResizeObserver(() => {
      this.updateDropdownSelection();
    });
  }

  ngOnInit(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true
    });

    const productId = this.route.snapshot.params['id'];
    this.productDetailService.productId.set(productId);
    if (productId) {
      this.getProductById(productId).subscribe(productDetail => {
        this.productDetail.set(productDetail);
        this.productModuleContent.set(productDetail.productModuleContent);
        this.metaProductJsonUrl = productDetail.metaProductJsonUrl;
        this.productDetailService.productNames.set(productDetail.names);
        this.installationCount = productDetail.installationCount;
        this.handleProductContentVersion();
        this.updateProductDetailActionType(productDetail);
        this.logoUrl = productDetail.logoUrl;
      });

      this.productFeedbackService.initFeedbacks();
      this.productStarRatingService.fetchData();
    }
    this.updateDropdownSelection();
  }

  onErrorLogo() {
    this.logoUrl = '/assets/images/misc/axonivy-logo-round.png';
  }

  handleProductContentVersion() {
    if (this.isEmptyProductContent()) {
      return;
    }
    this.selectedVersion = VERSION.displayPrefix.concat(
      this.convertTagToVersion(this.productModuleContent().tag)
    );
  }

  updateProductDetailActionType(productDetail: ProductDetail) {
    if (productDetail?.sourceUrl === undefined) {
      this.productDetailActionType.set(ProductDetailActionType.CUSTOM_SOLUTION);
    } else if (this.routingQueryParamService.isDesignerEnv()) {
      this.productDetailActionType.set(ProductDetailActionType.DESIGNER_ENV);
    } else {
      this.productDetailActionType.set(ProductDetailActionType.STANDARD)
    }
  }

  scrollToTop() {
    window.scrollTo({ left: 0, top: 0, behavior: 'instant' });
  }

  getProductById(productId: string): Observable<ProductDetail> {
    const targetVersion = this.routingQueryParamService.getDesignerVersionFromCookie();
    if (!targetVersion) {
      return this.productService.getProductDetails(productId);
    }

    return this.productService.getBestMatchProductDetailsWithVersion(
      productId,
      targetVersion
    );
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

    if (Object.keys(content).length === 0) {
      return false;
    }

    const conditions: { [key: string]: boolean } = {
      description:
        content.description !== null &&
        CommonUtils.isContentDisplayedBasedOnLanguage(
          content.description,
          this.languageService.selectedLanguage()
        ),
      demo:
        content.demo !== null &&
        CommonUtils.isContentDisplayedBasedOnLanguage(
          content.demo,
          this.languageService.selectedLanguage()
        ),
      setup:
        content.setup !== null &&
        CommonUtils.isContentDisplayedBasedOnLanguage(
          content.setup,
          this.languageService.selectedLanguage()
        ),
      dependency: content.isDependency
    };

    return conditions[value] ?? false;
  }

  isEmptyProductContent(): boolean {
    const content = this.productModuleContent();
    return !content || Object.keys(content).length === 0;
  }

  loadDetailTabs(selectedVersion: string) {
    let version = selectedVersion || this.productDetail().newestReleaseVersion;
    version = version.replace(VERSION.displayPrefix, '');
    this.productService
      .getProductDetailsWithVersion(this.productDetail().id, version)
      .subscribe(updatedProductDetail => {
        this.productModuleContent.set(
          updatedProductDetail.productModuleContent
        );
      });
  }

  onTabChange(event: string) {
    this.setActiveTab(event);
    this.isTabDropdownShown.update(value => !value);
    this.onTabDropdownShown();
  }

  getSelectedTabLabel() {
    return CommonUtils.getLabel(this.activeTab, PRODUCT_DETAIL_TABS);
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

    const savedTab = {
      productId: this.productDetail().id,
      savedActiveTab: this.activeTab
    };

    localStorage.setItem(STORAGE_ITEM, JSON.stringify(savedTab));
  }

  onShowInfoContent() {
    this.isDropdownOpen.update(value => !value);
  }

  onTabDropdownShown() {
    this.isTabDropdownShown.set(!this.isTabDropdownShown());
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    const formSelect =
      this.elementRef.nativeElement.querySelector('.form-select');
    if (
      formSelect &&
      !formSelect.contains(event.target) &&
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

  convertTagToVersion(tag: string): string {
    if (tag !== '' && tag.startsWith(VERSION.tagPrefix)) {
      return tag.substring(1);
    }
    return tag;
  }

  getDisplayedTabsSignal() {
    const displayedTabs: ItemDropdown[] = [];
    for (const detailTab of this.detailTabs) {
      if (this.getContent(detailTab.value)) {
        displayedTabs.push(detailTab);
        this.activeTab = displayedTabs[0].value;
      }
    }
    return displayedTabs;
  }

  getProductModuleContentValue(key: ItemDropdown): DisplayValue | null {
    type tabName = 'description' | 'demo' | 'setup';
    const value = key.value as tabName;
    return this.productModuleContent()[value];
  }
}
