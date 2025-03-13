import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  computed,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input,
  model,
  Output,
  Signal,
  signal,
  WritableSignal
} from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../product.service';
import { Tooltip } from 'bootstrap';
import { CommonDropdownComponent } from '../../../../shared/components/common-dropdown/common-dropdown.component';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ItemDropdown } from '../../../../shared/models/item-dropdown.model';
import { environment } from '../../../../../environments/environment';
import { SHOW_DEV_VERSION, VERSION } from '../../../../shared/constants/common.constant';
import { ProductDetailActionType } from '../../../../shared/enums/product-detail-action-type';
import { RoutingQueryParamService } from '../../../../shared/services/routing.query.param.service';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { CookieService } from 'ngx-cookie-service';
import { CommonUtils } from '../../../../shared/utils/common.utils';
import { ActivatedRoute, Router } from '@angular/router';
import { ROUTER } from '../../../../shared/constants/router.constant';
import { MatomoCategory, MatomoAction } from '../../../../shared/enums/matomo-tracking.enum';
import { MATOMO_TRACKING_ENVIRONMENT } from '../../../../shared/constants/matomo.constant';
import { MATOMO_DIRECTIVES } from 'ngx-matomo-client';
import { LoadingComponentId } from '../../../../shared/enums/loading-component-id';
import { LoadingService } from '../../../../core/services/loading/loading.service';
import { API_URI } from '../../../../shared/constants/api.constant';
import { HttpClient, HttpParams } from '@angular/common/http';
import { VersionDownload } from '../../../../shared/models/version-download.model';

const showDevVersionCookieName = 'showDevVersions';
const ARTIFACT_ZIP_URL = 'artifact/zip-file';
const TARGET_BLANK = '_blank';
const HTTP = 'http';
const DOC = '-doc';
const ZIP = '.zip';

@Component({
  selector: 'app-product-version-action',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    FormsModule,
    CommonDropdownComponent,
    LoadingSpinnerComponent,
    MATOMO_DIRECTIVES
  ],
  templateUrl: './product-detail-version-action.component.html',
  styleUrl: './product-detail-version-action.component.scss'
})
export class ProductDetailVersionActionComponent implements AfterViewInit {
  protected readonly environment = environment;
  @Output() installationCount = new EventEmitter<number>();
  @Input() productId!: string;
  @Input() isMavenDropins!: boolean;
  @Input() actionType!: ProductDetailActionType;
  @Input() product!: ProductDetail;
  protected ProductDetailActionType = ProductDetailActionType;
  protected MatomoCategory = MatomoCategory;
  protected MatomoAction = MatomoAction;
  trackedEnvironmentForMatomo = '';

  selectedVersion = model<string>('');
  versions: WritableSignal<string[]> = signal([]);
  versionDropdown: Signal<ItemDropdown[]> = computed(() => {
    return this.versions().map(version => ({
      value: version,
      label: version
    }));
  });
  metaDataJsonUrl = model<string>('');
  versionDropdownInDesigner: ItemDropdown[] = [];

  artifacts: WritableSignal<ItemDropdown[]> = signal([]);
  isDropDownDisplayed = signal(false);

  protected LoadingComponentId = LoadingComponentId;
  loadingContainerClasses =
    'd-flex justify-content-center position-absolute align-items-center w-100 h-100 fixed-top rounded overlay-background';
  designerVersion = '';
  selectedArtifactId: string | undefined = '';
  selectedArtifact: string | undefined = '';
  selectedArtifactName: string | undefined = '';
  versionMap: Map<string, ItemDropdown[]> = new Map();

  loadingService = inject(LoadingService);
  themeService = inject(ThemeService);
  productService = inject(ProductService);
  elementRef = inject(ElementRef);
  languageService = inject(LanguageService);
  routingQueryParamService = inject(RoutingQueryParamService);
  changeDetectorRef = inject(ChangeDetectorRef);
  cookieService = inject(CookieService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  httpClient = inject(HttpClient);

  isDevVersionsDisplayed: WritableSignal<boolean> = signal(
    this.getShowDevVersionFromCookie()
  );
  isCheckedAppForEngine!: boolean;

  ngAfterViewInit() {
    const tooltipTriggerList = [].slice.call(
      document.querySelectorAll('[data-bs-toggle="tooltip"]')
    );
    tooltipTriggerList.forEach(
      tooltipTriggerEl => new Tooltip(tooltipTriggerEl)
    );
  }

  onSelectVersion(version: string) {
    if (this.selectedVersion() !== version) {
      this.selectedVersion.set(version);
    }
    this.artifacts.set(this.versionMap.get(version) ?? []);
    this.updateSelectedArtifact(version);
  }

  private getShowDevVersionFromCookie() {
    return CommonUtils.getCookieValue(
      this.cookieService,
      SHOW_DEV_VERSION,
      false
    );
  }

  private updateSelectedArtifact(version: string) {
    this.artifacts().forEach(artifact => {
      if (artifact.name) {
        artifact.label = artifact.name;
      }
    });
    if (this.artifacts().length !== 0) {
      this.selectedArtifactId = this.artifacts()[0].artifactId ?? '';
      this.selectedArtifactName = this.artifacts()[0].name ?? '';
      this.selectedArtifact = this.artifacts()[0].downloadUrl ?? '';
    }
    this.addVersionParamToRoute(version);
  }

  addVersionParamToRoute(selectedVersion: string) {
    this.router
      .navigate([], {
        relativeTo: this.route,
        queryParams: { [ROUTER.VERSION]: selectedVersion },
        queryParamsHandling: 'merge'
      })
      .then();
  }

  onSelectVersionInDesigner(version: string) {
    this.selectedVersion.set(version);
  }

  onSelectArtifact(artifact: ItemDropdown) {
    this.selectedArtifactName = artifact.name;
    this.selectedArtifact = artifact.downloadUrl;
    this.selectedArtifactId = artifact.artifactId;
  }

  onShowDevVersion(event: Event) {
    event.preventDefault();
    this.isDevVersionsDisplayed.update(oldValue => !oldValue);
    this.cookieService.set(
      showDevVersionCookieName,
      this.isDevVersionsDisplayed().toString()
    );
    this.getVersionWithArtifact(true);
  }

  onShowVersionAndArtifact() {
    if (!this.isDropDownDisplayed() && this.artifacts().length === 0) {
      this.getVersionWithArtifact();
    }
    this.isDropDownDisplayed.set(!this.isDropDownDisplayed());
  }

  getVersionWithArtifact(ignoreRouteVersion = false) {
    this.sanitizeDataBeforeFetching();
    this.productService
      .sendRequestToProductDetailVersionAPI(
        this.productId,
        this.getShowDevVersionFromCookie(),
        this.designerVersion
      )
      .subscribe(data => {
        data.forEach(item => {
          const version = VERSION.displayPrefix.concat(item.version);
          this.versions.update(currentVersions => [
            ...currentVersions,
            version
          ]);

          if (!this.versionMap.get(version)) {
            this.versionMap.set(version, item.artifactsByVersion);
          }
        });
        if (this.versions().length !== 0) {
          this.onSelectVersion(
            this.getVersionFromRoute(ignoreRouteVersion) ?? this.versions()[0]
          );
        }
      });
  }

  getVersionFromRoute(ignoreRouteVersion: boolean): string | null {
    if (ignoreRouteVersion) {
      return null;
    }
    return this.route.snapshot.queryParams[ROUTER.VERSION] || null;
  }

  getVersionInDesigner(): void {
    if (this.versions().length === 0) {
      this.productService
        .sendRequestToGetProductVersionsForDesigner(this.productId)
        .subscribe(data => {
          const versionMap = data
            .map(dataVersionAndUrl => dataVersionAndUrl.version)
            .map(version => VERSION.displayPrefix.concat(version));
          data.forEach(dataVersionAndUrl => {
            const currentVersion = VERSION.displayPrefix.concat(
              dataVersionAndUrl.version
            );
            const versionAndUrl: ItemDropdown = {
              value: currentVersion,
              label: currentVersion,
              metaDataJsonUrl: dataVersionAndUrl.url
            };
            this.versionDropdownInDesigner.push(versionAndUrl);
          });
          this.versions.set(versionMap);
        });
    }
  }

  sanitizeDataBeforeFetching(): void {
    this.versions.set([]);
    this.artifacts.set([]);
    this.selectedArtifactId = '';
    this.selectedArtifact = '';
  }

  downloadArtifact(): void {
    if (!this.isCheckedAppForEngine || this.selectedArtifactId?.endsWith(DOC) || this.selectedArtifact?.endsWith(ZIP)) {
      let marketplaceServiceUrl = environment.apiUrl;
      if (!marketplaceServiceUrl.startsWith(HTTP)) {
        marketplaceServiceUrl = window.location.origin.concat(marketplaceServiceUrl);
      }
      const downloadUrl = `${marketplaceServiceUrl}/api/product-marketplace-data/version-download/${this.productId}?url=${this.selectedArtifact!}`;
      this.httpClient.get<VersionDownload>(downloadUrl).subscribe(
        (response) => {
          if (!response || !response.fileData) {
            console.error('No file data received');
            return;
          }
          this.installationCount.emit(response.installationCount);

          const fileName = this.selectedArtifact!.substring(this.selectedArtifact!.lastIndexOf("/") + 1);
          const byteCharacters = atob(response.fileData);
          const byteNumbers = new Array(byteCharacters.length);
          for (let i = 0; i < byteCharacters.length; i++) {
            byteNumbers[i] = byteCharacters.charCodeAt(i);
          }
          const byteArray = new Uint8Array(byteNumbers);
          const blob = new Blob([byteArray], { type: "application/octet-stream" });

          const blobUrl = URL.createObjectURL(blob);

          const a = document.createElement("a");
          a.href = blobUrl;
          a.download = fileName;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);

          setTimeout(() => URL.revokeObjectURL(blobUrl), 10000);
        });
    } else if (this.isCheckedAppForEngine) {
      let marketplaceServiceUrl = environment.apiUrl;
      if (!marketplaceServiceUrl.startsWith(HTTP)) {
        marketplaceServiceUrl = window.location.origin.concat(marketplaceServiceUrl);
      }
      const version = this.selectedVersion().replace(VERSION.displayPrefix, '');
      const params = new HttpParams()
        .set(ROUTER.VERSION, version)
      window.open(`${marketplaceServiceUrl}/${API_URI.PRODUCT_DETAILS}/${this.productId}/${ARTIFACT_ZIP_URL}?${params.toString()}`, TARGET_BLANK);
      this.onUpdateInstallationCount();
    } else {
      this.onUpdateInstallationCount();
      return;
    }
  }

  onUpdateInstallationCount(): void {
    this.productService
      .sendRequestToUpdateInstallationCount(
        this.productId,
        this.routingQueryParamService.getDesignerVersionFromSessionStorage()
      )
      .subscribe((data: number) => this.installationCount.emit(data));
  }

  onUpdateInstallationCountForDesigner(): void {
    this.onUpdateInstallationCount();
  }

  onNavigateToContactPage(): void {
    window.open(
      `https://www.axonivy.com/marketplace/contact/?market_solutions=${this.productId}`,
      '_blank'
    );
  }

  getTrackingEnvironmentBasedOnActionType() {
    switch (this.actionType) {
      case ProductDetailActionType.STANDARD:
        return MATOMO_TRACKING_ENVIRONMENT.standard;
      case ProductDetailActionType.DESIGNER_ENV:
        return MATOMO_TRACKING_ENVIRONMENT.designerEnv;
      case ProductDetailActionType.CUSTOM_SOLUTION:
        return MATOMO_TRACKING_ENVIRONMENT.customSolution;
      default:
        return '';
    }
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent): void {
    const downloadDialog = this.elementRef.nativeElement.querySelector(
      '#download-dropdown-menu'
    );
    if (
      this.isDropDownDisplayed() &&
      downloadDialog &&
      !downloadDialog.contains(event.target)
    ) {
      this.isDropDownDisplayed.set(!this.isDropDownDisplayed());
    }
  }
}
