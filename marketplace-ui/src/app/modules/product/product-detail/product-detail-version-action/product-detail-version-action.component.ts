import {
  AfterViewInit,
  Component,
  ElementRef, EventEmitter,
  HostListener,
  inject,
  Input,
  model, Output,
  signal,
  WritableSignal
} from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../product.service';
import { Artifact } from '../../../../shared/models/vesion-artifact.model';
import { Tooltip } from 'bootstrap';
import { ProductDetailService } from '../product-detail.service';
import { CookieManagementService } from '../../../../cookie.management.service';

const delayTimeBeforeHideMessage = 2000;
@Component({
  selector: 'app-product-version-action',
  standalone: true,
  imports: [CommonModule, TranslateModule, FormsModule],
  templateUrl: './product-detail-version-action.component.html',
  styleUrl: './product-detail-version-action.component.scss'
})
export class ProductDetailVersionActionComponent implements AfterViewInit {
  @Output() installationCount = new EventEmitter<number>();
  @Input()
  productId!: string;
  selectedVersion = model<string>('');
  versions: WritableSignal<string[]> = signal([]);
  artifacts: WritableSignal<Artifact[]> = signal([]);
  isDevVersionsDisplayed = signal(false);
  isDropDownDisplayed = signal(false);
  isVersionsDropDownShow = signal(false);
  isDesignerEnvironment = signal(false);
  isInvalidInstallationEnvironment = signal(false);
  designerVersion = '';
  selectedArtifact = '';
  versionMap: Map<string, Artifact[]> = new Map();

  cookieService = inject(CookieManagementService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  productService = inject(ProductService);
  productDetailService = inject(ProductDetailService);
  elementRef = inject(ElementRef);

  ngAfterViewInit() {
    const tooltipTriggerList = [].slice.call(
      document.querySelectorAll('[data-bs-toggle="tooltip"]')
    );
    tooltipTriggerList.forEach(
      tooltipTriggerEl => new Tooltip(tooltipTriggerEl)
    );
    this.isDesignerEnvironment.set(this.cookieService.isDesignerEnv());
  }

  onShowVersions() {
    this.isVersionsDropDownShow.set(!this.isVersionsDropDownShow());
  }

  getInstallationTooltipText() {
    return `<p class="text-primary">Please open the
        <a href="https://market.axonivy.com" class="ivy__link">Axon Ivy Market</a>
        inside your
        <a class="ivy__link" href="https://developer.axonivy.com/download">Axon Ivy Designer</a>
        (minimum version 9.2.0)</p>`;
  }

  onSelectVersion() {
    this.artifacts.set(this.versionMap.get(this.selectedVersion()) ?? []);

    if (this.artifacts().length !== 0) {
      this.selectedArtifact = this.artifacts()[0].downloadUrl;
    }
  }

  onShowDevVersion(event: Event) {
    event.preventDefault();
    this.isDevVersionsDisplayed.set(!this.isDevVersionsDisplayed());
    this.getVersionWithArtifact();
  }

  onShowVersionAndArtifact() {
    if (!this.isDropDownDisplayed() && this.artifacts().length === 0) {
      this.getVersionWithArtifact();
    }
    this.isDropDownDisplayed.set(!this.isDropDownDisplayed());
  }

  getVersionWithArtifact() {
    this.sanitizeDataBeforFetching();

    this.productService
      .sendRequestToProductDetailVersionAPI(
        this.productId,
        this.isDevVersionsDisplayed(),
        this.designerVersion
      )
      .subscribe(data => {
        data.forEach(item => {
          const version = 'Version '.concat(item.version);
          this.versions.update(currentVersions => [
            ...currentVersions,
            version
          ]);
          if (!this.versionMap.get(version)) {
            this.versionMap.set(version, item.artifactsByVersion);
          }
        });
        if (this.versions().length !== 0) {
          this.selectedVersion.set(this.versions()[0]);
          this.onSelectVersion();
        }
      });
  }

  sanitizeDataBeforFetching() {
    this.versions.set([]);
    this.artifacts.set([]);
    this.selectedArtifact = '';
    this.selectedVersion.set('');
  }

  downloadArifact() {
    this.onUpdateInstallationCount();
    const newTab = window.open(this.selectedArtifact, '_blank');
    if (newTab) {
      newTab.blur();
    }
    window.focus();
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    if (
      !this.elementRef.nativeElement.contains(event.target) &&
      this.isVersionsDropDownShow()
    ) {
      this.onShowVersions();
    }
  }

  onUpdateInstallationCount() {
    this.productService
      .sendRequestToUpdateInstallationCount(this.productId)
      .subscribe((data: number) => this.installationCount.emit(data));
  }

  onUpdateInstallationCountForDesigner() {
    if (this.isDesignerEnvironment()) {
      this.onUpdateInstallationCount();
    }
  }
}
