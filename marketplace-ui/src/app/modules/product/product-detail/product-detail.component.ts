import { ProductDetail } from './../../../shared/models/product-detail.model';
import {
  CommonModule,
  isPlatformBrowser,
  NgOptimizedImage
} from '@angular/common';
import MarkdownIt from 'markdown-it';
import {
  Component,
  ElementRef,
  HostListener,
  Inject,
  PLATFORM_ID,
  Signal,
  WritableSignal,
  computed,
  inject,
  signal
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbAccordionModule, NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { forkJoin, Subscription } from 'rxjs';
import { AuthService } from '../../../auth/auth.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { CommonDropdownComponent } from '../../../shared/components/common-dropdown/common-dropdown.component';
import {
  DEFAULT_IMAGE_URL,
  GITHUB_PULL_REQUEST_NUMBER_REGEX,
  OG_TITLE_KEY,
  PRODUCT_DETAIL_TABS,
  RATING_LABELS_BY_TYPE,
  TAB_PREFIX,
  UNESCAPE_GITHUB_CONTENT_REGEX,
  VERSION
} from '../../../shared/constants/common.constant';
import { ItemDropdown } from '../../../shared/models/item-dropdown.model';
import { ProductModuleContent } from '../../../shared/models/product-module-content.model';
import { ProductTypeIconPipe } from '../../../shared/pipes/icon.pipe';
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
import { CookieService } from 'ngx-cookie-service';
import { ROUTER } from '../../../shared/constants/router.constant';
import { SafeHtml, Title, DomSanitizer, Meta } from '@angular/platform-browser';
import { API_URI } from '../../../shared/constants/api.constant';
import { EmptyProductDetailPipe } from '../../../shared/pipes/empty-product-detail.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { LoadingService } from '../../../core/services/loading/loading.service';
import { ProductRelease } from '../../../shared/models/apis/product-release.model';
import LinkifyIt from 'linkify-it';
import { ProductReleaseSafeHtml } from '../../../shared/models/product-release-safe-html.model';
import { HistoryService } from '../../../core/services/history/history.service';
import { TypeOption } from '../../../shared/enums/type-option.enum';
import { SortOption } from '../../../shared/enums/sort-option.enum';
import { MarkdownService } from '../../../shared/services/markdown.service';
import { full } from 'markdown-it-emoji';

export interface DetailTab {
  activeClass: string;
  tabId: string;
  value: string;
  label: string;
}

const STORAGE_ITEM = 'activeTab';
const DEFAULT_ACTIVE_TAB = 'description';
const GITHUB_BASE_URL = 'https://github.com/';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    ProductDetailVersionActionComponent,
    CommonModule,
    ProductStarRatingNumberComponent,
    TranslateModule,
    ProductDetailInformationTabComponent,
    ProductDetailMavenContentComponent,
    NgbNavModule,
    MultilingualismPipe,
    ProductTypePipe,
    ProductDetailFeedbackComponent,
    ProductInstallationCountActionComponent,
    ProductTypeIconPipe,
    CommonDropdownComponent,
    NgOptimizedImage,
    EmptyProductDetailPipe,
    LoadingSpinnerComponent,
    NgbAccordionModule
  ],
  providers: [ProductService],
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
  cookieService = inject(CookieService);
  routingQueryParamService = inject(RoutingQueryParamService);
  loadingService = inject(LoadingService);
  historyService = inject(HistoryService);
  markdownService = inject(MarkdownService);
  subscriptions: Subscription[] = [];

  protected LoadingComponentId = LoadingComponentId;
  protected ProductDetailActionType = ProductDetailActionType;

  resizeObserver?: ResizeObserver;
  productDetail: WritableSignal<ProductDetail> = signal({} as ProductDetail);
  productModuleContent: WritableSignal<ProductModuleContent> = signal(
    {} as ProductModuleContent
  );
  productDetailActionType = signal(ProductDetailActionType.STANDARD);
  detailTabs = PRODUCT_DETAIL_TABS;
  activeTab = '';
  displayedTabsSignal: Signal<ItemDropdown[]> = computed(() => {
    this.languageService.selectedLanguage();
    this.getReadmeContent();
    return this.getDisplayedTabsSignal();
  });
  isDropdownOpen: WritableSignal<boolean> = signal(false);
  selectedVersion = '';
  metaProductJsonUrl: string | undefined = '';
  showPopup!: boolean;
  isMobileMode = signal<boolean>(false);
  installationCount = 0;
  logoUrl = DEFAULT_IMAGE_URL;
  md: MarkdownIt = new MarkdownIt();
  productReleaseSafeHtmls: ProductReleaseSafeHtml[] = [];
  loadedReadmeContent: { [key: string]: SafeHtml } = {};
  isBrowser: boolean;
  meta = inject(Meta);

  @HostListener('window:popstate', ['$event'])
  onPopState() {
    this.activeTab = window.location.hash.split('#tab-')[1];
    if (this.activeTab === undefined) {
      this.activeTab = DEFAULT_ACTIVE_TAB;
    }
    this.updateDropdownSelection();
  }

  constructor(
    private readonly titleService: Title,
    private readonly sanitizer: DomSanitizer,
    @Inject(PLATFORM_ID) private readonly platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    if (this.isBrowser) {
      this.scrollToTop();
      this.resizeObserver = new ResizeObserver(() => {
        this.updateDropdownSelection();
      });
    }
  }

  ngOnInit(): void {
    const productId = this.route.snapshot.params[ROUTER.ID];
    this.productDetailService.productId.set(productId);
    const productDetail = this.route.snapshot.data[
      ROUTER.PRODUCT_DETAIL
    ] as ProductDetail;
    
    this.handleProductDetailLoad(productId, productDetail);
  }

  private handleProductDetailLoad(productId: string, productDetail: ProductDetail): void {
    if (this.isBrowser) {
      forkJoin({
        userFeedback: this.productFeedbackService.findProductFeedbackOfUser(),
        productFeedBack:
          this.productFeedbackService.getInitFeedbacksObservable(),
        rating: this.productStarRatingService.getRatingObservable(productId),
        changelogs: this.productService.getProductChangelogs(productId)
      }).subscribe(res => {
        this.setupMarkdownParser(productDetail.sourceUrl);

        const gitHubReleaseModelList =
          res.changelogs?._embedded?.gitHubReleaseModelList ?? [];
        if (gitHubReleaseModelList.length > 0) {
          this.productReleaseSafeHtmls = this.renderChangelogContent(
            gitHubReleaseModelList
          );
        }

        this.handleProductDetail(productDetail);
        this.getReadmeContent();
        this.productFeedbackService.handleFeedbackApiResponse(
          res.productFeedBack
        );
        this.updateDropdownSelection();
        this.checkMediaSize();

        this.handlePopupLogic();
        this.loadingService.hideLoading(LoadingComponentId.DETAIL_PAGE);
        this.navigateToProductDetailsWithTabFragment();
      });
    }
  }

  private setupMarkdownParser(sourceUrl: string): void {
    this.md
      .use(full)
      .use(this.linkifyPullRequests, sourceUrl, GITHUB_PULL_REQUEST_NUMBER_REGEX)
      .set({
        typographer: true,
        linkify: true
      })
      .enable(['smartquotes', 'replacements', 'image']);
  }

  private handlePopupLogic(): void {
    this.route.queryParams.subscribe(params => {
      this.showPopup = params['showPopup'] === 'true';
      if (this.showPopup && this.authService.getToken()) {
        this.appModalService
          .openAddFeedbackDialog()
          .then(() => this.removeQueryParam())
          .catch(() => this.removeQueryParam());
      }
    });
  }

  handleProductDetail(productDetail: ProductDetail): void {
    this.productDetail.set(productDetail);
    this.productModuleContent.set(productDetail.productModuleContent);
    this.metaProductJsonUrl = productDetail.metaProductJsonUrl;
    this.productDetailService.productNames.set(productDetail.names);
    this.productDetailService.productLogoUrl.set(productDetail.logoUrl);
    this.installationCount = productDetail.installationCount;
    this.handleProductContentVersion();
    this.updateProductDetailActionType(productDetail);
    this.logoUrl = productDetail.logoUrl;
    const ratingLabels = RATING_LABELS_BY_TYPE.find(
      button => button.type === productDetail.type
    );
    if (ratingLabels !== undefined) {
      this.productDetailService.ratingBtnLabel.set(ratingLabels.btnLabel);
      this.productDetailService.noFeedbackLabel.set(
        ratingLabels.noFeedbackLabel
      );
    }
  }

  onClickingBackToHomepageButton(): void {
    if (this.historyService.isLastSearchChanged()) {
      this.navigateToHomePageWithLastSearch();
    } else {
      this.router.navigate([API_URI.APP]);
    }
  }

  navigateToHomePageWithLastSearch() {
    const queryParams: Record<string, string | SortOption | TypeOption> = {};
    if (this.historyService.lastSearchType() !== TypeOption.All_TYPES) {
      queryParams['type'] = this.historyService.lastSearchType();
    }

    if (this.historyService.lastSortOption() !== SortOption.STANDARD) {
      queryParams['sort'] = this.historyService.lastSortOption();
    }

    if (this.historyService.lastSearchText() !== '') {
      queryParams['search'] = this.historyService.lastSearchText();
    }

    this.router.navigate([API_URI.APP], {
      relativeTo: this.route,
      queryParamsHandling: 'merge',
      queryParams
    });
  }

  onLogoError(): void {
    this.logoUrl = DEFAULT_IMAGE_URL;
  }

  handleProductContentVersion(): void {
    if (this.isEmptyProductContent()) {
      return;
    }
    this.selectedVersion = VERSION.displayPrefix.concat(
      this.productModuleContent().version
    );
  }

  updateProductDetailActionType(productDetail: ProductDetail): void {
    if (productDetail?.sourceUrl === undefined) {
      this.productDetailActionType.set(ProductDetailActionType.CUSTOM_SOLUTION);
    } else if (this.routingQueryParamService.isDesignerEnv()) {
      this.productDetailActionType.set(ProductDetailActionType.DESIGNER_ENV);
    } else {
      this.productDetailActionType.set(ProductDetailActionType.STANDARD);
    }
  }

  scrollToTop(): void {
    window.scrollTo({ left: 0, top: 0, behavior: 'instant' });
  }

  getContent(value: string): boolean {
    const content = this.productModuleContent();

    if (!content || Object.keys(content).length === 0) {
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
      dependency: content.isDependency,
      changelog: this.productReleaseSafeHtmls != null && this.productReleaseSafeHtmls.length !== 0
    };

    return conditions[value] ?? false;
  }

  isEmptyProductContent(): boolean {
    const content = this.productModuleContent();
    return !content || Object.keys(content).length === 0;
  }

  loadDetailTabs(selectedVersion: string): void {
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

  onTabChange(event: string): void {
    this.setActiveTab(event);
  }

  getSelectedTabLabel(): string {
    return CommonUtils.getLabel(this.activeTab, PRODUCT_DETAIL_TABS);
  }

  updateDropdownSelection(): void {
    const dropdown = document.getElementById(
      'tab-group-dropdown'
    ) as HTMLSelectElement;
    if (dropdown) {
      dropdown.value = this.activeTab;
    }
  }

  setActiveTab(tab: string): void {
    this.router.navigate([], {
      fragment: TAB_PREFIX + tab,
      queryParamsHandling: 'preserve',
      replaceUrl: true
    });

    this.activeTab = tab;
    this.updateDropdownSelection();

    const savedTab = {
      productId: this.productDetail().id,
      savedActiveTab: this.activeTab
    };

    localStorage.setItem(STORAGE_ITEM, JSON.stringify(savedTab));
  }

  onShowInfoContent(): void {
    this.isDropdownOpen.update(value => !value);
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent): void {
    if (!this.isDropdownOpen()) {
      return;
    }

    const target = event.target as HTMLElement;
    if (target) {
      const isClickInside =
        target.closest('.info-dropdown') ||
        target.closest('#info-content-dropdown__icon');

      if (!isClickInside) {
        this.onShowInfoContent();
      }
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(): void {
    this.checkMediaSize();
  }

  checkMediaSize(): void {
    const mediaQuery = window.matchMedia('(max-width: 767px)');
    if (mediaQuery.matches) {
      this.isMobileMode.set(true);
    } else {
      this.isMobileMode.set(false);
    }
  }

  onClickRateBtn(): void {
    const productId = this.productDetailService.productId();
    if (this.authService.getToken()) {
      this.appModalService.openAddFeedbackDialog();
    } else {
      this.authService.redirectToGitHub(productId);
    }
  }

  receiveInstallationCountData(data: number): void {
    this.installationCount = data;
  }

  private removeQueryParam(): void {
    this.router.navigate([], {
      queryParams: { showPopup: null },
      queryParamsHandling: 'merge'
    });
  }

  updateWebBrowserTitle(names: DisplayValue): void {
    if (names !== undefined) {
      const title = names[this.languageService.selectedLanguage()];
      this.titleService.setTitle(title);
      this.meta.updateTag({ property: OG_TITLE_KEY, content: title });
    }
  }

  getDisplayedTabsSignal(): ItemDropdown[] {
    this.updateWebBrowserTitle(this.productDetail().names);
    const displayedTabs: ItemDropdown[] = [];
    for (const detailTab of this.detailTabs) {
      if (this.getContent(detailTab.value)) {
        displayedTabs.push(detailTab);
      }
    }

    return displayedTabs;
  }

  getProductModuleContentValue(key: ItemDropdown): DisplayValue | null {
    type tabName = 'description' | 'demo' | 'setup';
    const value = key.value as tabName;
    return this.productModuleContent()?.[value] ?? null;
  }

  getReadmeContent() {
    this.detailTabs.forEach(tab => {
      const contentValue = this.getProductModuleContentValue(tab);
      if (contentValue) {
        const translatedContent = new MultilingualismPipe().transform(
          contentValue,
          this.languageService.selectedLanguage()
        );

        this.loadedReadmeContent[tab.value] = this.renderGithubAlert(translatedContent);
      }
    });
  }

  renderGithubAlert(value: string): SafeHtml {
    const result = this.markdownService.parseMarkdown(value);
    return this.sanitizer.bypassSecurityTrustHtml(result);
  }

  renderChangelogContent(releases: ProductRelease[]): ProductReleaseSafeHtml[] {
    return releases.map(release => {
      return {
        name: release.name,
        body: this.bypassSecurityTrustHtml(release.body),
        publishedAt: release.publishedAt,
        htmlUrl: release.htmlUrl,
        isLatestRelease: release.latestRelease
      };
    });
  }

  private bypassSecurityTrustHtml(value: string): SafeHtml {
    const markdownContent = this.md.render(value.replace(UNESCAPE_GITHUB_CONTENT_REGEX, '$1'));
    return this.sanitizer.bypassSecurityTrustHtml(markdownContent);
  }

  linkifyPullRequests(md: MarkdownIt, sourceUrl: string, prNumberRegex: RegExp) {
    md.renderer.rules.text = (tokens, idx) => {
      const content = tokens[idx].content;
      const linkify = new LinkifyIt();
      const matches = linkify.match(content);

      if (!matches) {
        return content;
      }

      let result = content;

      matches.forEach(match => {
        const url = match.url;

        if (url.startsWith(`${sourceUrl}/compare/`)) {
          return;
        }
        if (url.startsWith(sourceUrl)) {
          const pullNumberMatch = prNumberRegex.exec(url);
          let pullNumber = null;

          if (pullNumberMatch) {
            pullNumber = pullNumberMatch[1];
            const start = match.index;
            const end = start + match.lastIndex - match.index;
            const link = `#${pullNumber}`;

            result = result.slice(0, start) + link + result.slice(end);
          }
        } else if (url.startsWith(GITHUB_BASE_URL)) {
          const username = url.replace(GITHUB_BASE_URL, '');

          const mention = `@${username}`;
          result = result.replace(url, mention);
        } else {
          return;
        }
      });

      return result;
    };
  }

  navigateToProductDetailsWithTabFragment(): void {
    this.subscriptions.push(
      this.route.fragment.subscribe(fragment => {
        const tabValue = this.getTabValueFromFragment(fragment);
        this.setActiveTab(tabValue);
      })
    );
  }

  getTabValueFromFragment(fragment: string | null): string {
    const isValidTab = this.displayedTabsSignal().some(tab => tab.tabId === fragment);
    const tabId = fragment?.replace(TAB_PREFIX, '');
    if (isValidTab && tabId) {
      return tabId;
    }
    return PRODUCT_DETAIL_TABS[0].value;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => {
      sub.unsubscribe();
    });
  }
}
