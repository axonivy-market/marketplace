import { CommonModule } from '@angular/common';
import {
  Component,
  inject,
  Input,
  OnChanges,
  SimpleChange,
  SimpleChanges
} from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ProductDetailService } from '../product-detail.service';
import { VERSION, VERSION_PARAM } from '../../../../shared/constants/common.constant';
import { LoadingService } from '../../../../core/services/loading/loading.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { IsEmptyObjectPipe } from '../../../../shared/pipes/is-empty-object.pipe';
import { LoadingComponentId } from '../../../../shared/enums/loading-component-id';
import { Router, ActivatedRoute } from '@angular/router';
import { ROUTER } from '../../../../shared/constants/router.constant';
const SELECTED_VERSION = 'selectedVersion';
const PRODUCT_DETAIL = 'productDetail';
const SHIELDS_BADGE_BASE_URL = 'https://img.shields.io/github/actions/workflow/status';
const SHIELDS_WORKFLOW = 'ci.yml';
const BRANCH = 'master';
@Component({
  selector: 'app-product-detail-information-tab',
  standalone: true,
  imports: [CommonModule, TranslateModule, IsEmptyObjectPipe],
  templateUrl: './product-detail-information-tab.component.html',
  styleUrl: './product-detail-information-tab.component.scss'
})
export class ProductDetailInformationTabComponent implements OnChanges {
  @Input()
  productDetail!: ProductDetail;
  @Input()
  selectedVersion!: string;
  protected LoadingComponentId = LoadingComponentId;
  externalDocumentLink = '';
  displayVersion = '';
  displayExternalDocName: string | null = '';
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  productDetailService = inject(ProductDetailService);
  loadingService = inject(LoadingService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  shieldsBadgeUrl = '';
  repoName = '';

  ngOnInit(): void {
    this.displayVersion = this.extractVersionValue(this.selectedVersion);
    this.shieldsBadgeUrl = this.getShieldsBadgeUrl();
  }

  ngOnChanges(changes: SimpleChanges): void {
    let version = '';
    if (this.isVersionUnchangedOrFirstChange(changes[SELECTED_VERSION])) {
      return;
    }
    version = this.extractVersionValue(
      this.route.snapshot.queryParamMap.get(VERSION_PARAM) ?? this.selectedVersion
    );
    // Invalid version
    if (version === undefined || version === '') {
      return;
    }
    this.productDetailService
      .getExternalDocumentForProductByVersion(this.productDetail.id, version)
      .subscribe({
        next: response => {
          if (response) {
            this.externalDocumentLink = response.relativeLink;
            this.displayExternalDocName = response.artifactName;
            version = response.version;
          } else {
            this.resetValues();
            version = this.extractVersionValue(this.selectedVersion);
          }
          this.displayVersion = version;
          this.shieldsBadgeUrl = this.getShieldsBadgeUrl();
          this.addVersionParamToRoute(version);
        },
        error: () => {
          this.resetValues();
          this.displayVersion = this.extractVersionValue(this.selectedVersion);
          this.shieldsBadgeUrl = this.getShieldsBadgeUrl();
          this.addVersionParamToRoute(version);
        }
      });
  }
  getShieldsBadgeUrl(): string {
    if (!this.productDetail?.statusBadgeUrl) {
      return '';
    }
    const url = new URL(this.productDetail.statusBadgeUrl);
    const pathParts = url.pathname.split('/').filter(part => part.length > 0);
    const owner = pathParts[0]; 
    this.repoName = pathParts[1]; 
    return `${SHIELDS_BADGE_BASE_URL}/${owner}/${this.repoName}/${SHIELDS_WORKFLOW}?branch=${BRANCH}`;
  }

  isVersionUnchangedOrFirstChange(change: SimpleChange | undefined): boolean {
    return (
      !!change &&
      (change.currentValue === change.previousValue || change.firstChange)
    );
  }

  resetValues() {
    this.externalDocumentLink = '';
    this.displayExternalDocName = '';
  }

  extractVersionValue(versionDisplayName: string) {
    return versionDisplayName.replace(VERSION.displayPrefix, '');
  }

  //  To ensure the function always returns a boolean, you can explicitly coerce the result into a boolean using the !! operator or default it to false
  //  Adding !! in case of changedProduct is undefined, it will return false instead of returning undefined
  isProductChanged(changedProduct: SimpleChange) {
    return !!(changedProduct?.previousValue &&
      Object.keys(changedProduct.previousValue).length > 0 &&
      changedProduct.currentValue !== changedProduct.previousValue
    );
  }
  onBadgeClick() {
    if (this.repoName) {
      this.router.navigate(['/monitoring'], { 
        queryParams: { 
          search: this.repoName
        } 
      });
    } else {
      this.router.navigate(['/monitoring']);
    }
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
}
