import {
  AfterViewInit,
  Component,
  computed,
  ElementRef,
  EventEmitter,
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
import { VERSION } from '../../../../shared/constants/common.constant';
import { ProductDetailActionType } from '../../../../shared/enums/product-detail-action-type';
import { RoutingQueryParamService } from '../../../../shared/services/routing.query.param.service';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { CookieService } from 'ngx-cookie-service';
import { CommonUtils } from '../../../../shared/utils/common.utils';
import { ActivatedRoute, Router } from '@angular/router';

const delayTimeBeforeHideMessage = 2000;

@Component({
  selector: 'app-product-version-action',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    FormsModule,
    CommonDropdownComponent,
    LoadingSpinnerComponent
  ],
  templateUrl: './product-detail-version-action.component.html',
  styleUrl: './product-detail-version-action.component.scss'
})
export class ProductDetailVersionActionComponent implements AfterViewInit {
  readonly SHOW_DEV_VERSION_COOKIE_NAME: string = 'showDevVersions';
  readonly VERSION_PARAM: string = 'version';
  protected readonly environment = environment;
  @Output() installationCount = new EventEmitter<number>();
  @Input() productId!: string;
  @Input() actionType!: ProductDetailActionType;
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
  isInvalidInstallationEnvironment = signal(false);
  isArtifactLoading = signal(false);
  designerVersion = '';
  selectedArtifact: string | undefined = '';
  selectedArtifactName: string | undefined = '';
  versionMap: Map<string, ItemDropdown[]> = new Map();

  themeService = inject(ThemeService);
  productService = inject(ProductService);
  elementRef = inject(ElementRef);
  languageService = inject(LanguageService);
  routingQueryParamService = inject(RoutingQueryParamService);
  cookieService = inject(CookieService);
  router = inject(Router);
  route = inject(ActivatedRoute);

  isDevVersionsDisplayed: WritableSignal<boolean> = signal(CommonUtils.getCookieValue(this.cookieService, this.SHOW_DEV_VERSION_COOKIE_NAME, false));

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

  private updateSelectedArtifact(version: string) {
    this.artifacts().forEach(artifact => {
      if (artifact.name) {
        artifact.label = artifact.name;
      }
    });
    if (this.artifacts().length !== 0) {
      this.selectedArtifactName = this.artifacts()[0].name ?? '';
      this.selectedArtifact = this.artifacts()[0].downloadUrl ?? '';
    }
    this.addVersionParamToRoute(version);
  }

  addVersionParamToRoute(selectedVersion: string) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { [this.VERSION_PARAM]: selectedVersion },
      queryParamsHandling: 'merge'
    }).then();
  }

  onSelectVersionInDesigner(version: string) {
    this.selectedVersion.set(version);
  }

  onSelectArtifact(artifact: ItemDropdown) {
    this.selectedArtifactName = artifact.name;
    this.selectedArtifact = artifact.downloadUrl;
  }

  onShowDevVersion(event: Event) {
    event.preventDefault();
    this.isDevVersionsDisplayed.update(oldValue => !oldValue);
    this.getVersionWithArtifact();
    this.cookieService.set(this.SHOW_DEV_VERSION_COOKIE_NAME, this.isDevVersionsDisplayed().toString());
  }

  onShowVersionAndArtifact() {
    if (!this.isDropDownDisplayed() && this.artifacts().length === 0) {
      this.getVersionWithArtifact();
    }
    this.isDropDownDisplayed.set(!this.isDropDownDisplayed());
  }

  getVersionWithArtifact() {
    this.isArtifactLoading.set(true);
    this.sanitizeDataBeforeFetching();
    this.productService
      .sendRequestToProductDetailVersionAPI(
        this.productId,
        CommonUtils.getCookieValue(this.cookieService, this.SHOW_DEV_VERSION_COOKIE_NAME, false),
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
          this.onSelectVersion(this.getVersionFromRoute() ?? this.versions()[0]);
          // this.artifacts.set(this.versionMap.get(this.selectedVersion()) ?? []);
          // this.updateSelectedArtifact();
        }
        this.isArtifactLoading.set(false);
      });
  }

  getVersionFromRoute(): string | null {
    return this.route.snapshot.queryParams[this.VERSION_PARAM] || null;
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

  sanitizeDataBeforeFetching() {
    this.versions.set([]);
    this.artifacts.set([]);
    this.selectedArtifact = '';
  }

  downloadArtifact() {
    this.onUpdateInstallationCount();
    const newTab = window.open(this.selectedArtifact, '_blank');
    if (newTab) {
      newTab.blur();
    }
    window.focus();
  }

  onUpdateInstallationCount() {
    this.productService
      .sendRequestToUpdateInstallationCount(
        this.productId,
        this.routingQueryParamService.getDesignerVersionFromCookie()
      )
      .subscribe((data: number) => this.installationCount.emit(data));
  }

  onUpdateInstallationCountForDesigner() {
    this.onUpdateInstallationCount();
  }

  onNavigateToContactPage() {
    const newTab = window.open(
      `https://www.axonivy.com/marketplace/contact/?market_solutions=${this.productId}`,
      '_blank'
    );
    if (newTab) {
      newTab.blur();
    }
    window.focus();
  }
}
