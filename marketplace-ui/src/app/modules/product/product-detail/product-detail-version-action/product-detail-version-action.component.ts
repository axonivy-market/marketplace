import {
  AfterViewInit,
  Component, computed,
  ElementRef, EventEmitter,
  inject,
  Input,
  model, Output, Signal,
  signal,
  WritableSignal
} from '@angular/core';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../product.service';
import { Tooltip } from 'bootstrap';
import { ProductDetailService } from '../product-detail.service';
import { RoutingQueryParamService } from '../../../../shared/services/routing.query.param.service';
import { CommonDropdownComponent } from '../../../../shared/components/common-dropdown/common-dropdown.component';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ItemDropdown } from '../../../../shared/models/item-dropdown.model';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { environment } from '../../../../../environments/environment';
import { VERSION } from '../../../../shared/constants/common.constant';

const delayTimeBeforeHideMessage = 2000;
@Component({
  selector: 'app-product-version-action',
  standalone: true,
  imports: [CommonModule, TranslateModule, FormsModule, CommonDropdownComponent],
  templateUrl: './product-detail-version-action.component.html',
  styleUrl: './product-detail-version-action.component.scss'
})
export class ProductDetailVersionActionComponent implements AfterViewInit {
  protected readonly environment = environment;
  @Output() installationCount = new EventEmitter<number>();
  @Input() productId!: string;

  @Input() product!: ProductDetail;
  selectedVersion = model<string>('');
  versions: WritableSignal<string[]> = signal([]);
  versionDropdown : Signal<ItemDropdown[]> = computed(() => {
    return this.versions().map(version => ({
      value: version,
      label: version,
    }));
  });
  metaDataJsonUrl = model<string>('');
  versionDropdownInDesigner: ItemDropdown[] = [];

  artifacts: WritableSignal<ItemDropdown[]> = signal([]);
  isDevVersionsDisplayed = signal(false);
  isDropDownDisplayed = signal(false);
  isDesignerEnvironment = signal(false);
  isInvalidInstallationEnvironment = signal(false);
  designerVersion = '';
  selectedArtifact: string | undefined = '';
  selectedArtifactName:string | undefined = '';
  versionMap: Map<string, ItemDropdown[]> = new Map();

  routingQueryParamService = inject(RoutingQueryParamService);
  themeService = inject(ThemeService);
  productService = inject(ProductService);
  productDetailService = inject(ProductDetailService);
  elementRef = inject(ElementRef);
  languageService = inject(LanguageService);

  ngAfterViewInit() {
    const tooltipTriggerList = [].slice.call(
      document.querySelectorAll('[data-bs-toggle="tooltip"]')
    );
    tooltipTriggerList.forEach(
      tooltipTriggerEl => new Tooltip(tooltipTriggerEl)
    );
    this.isDesignerEnvironment.set(
      this.routingQueryParamService.isDesignerEnv()
    );

  }

  getInstallationTooltipText() {
    return `<p class="text-primary">Please open the
        <a href="https://market.axonivy.com" class="ivy__link">Axon Ivy Market</a>
        inside your
        <a class="ivy__link" href="https://developer.axonivy.com/download">Axon Ivy Designer</a>
        (minimum version 9.2.0)</p>`;
  }

  onSelectVersion(version : string) {
    this.selectedVersion.set(version);
    this.artifacts.set(this.versionMap.get(this.selectedVersion()) ?? []);
    this.artifacts().forEach(artifact => {
      if(artifact.name) {
        artifact.label = artifact.name;
      }
    });
    if (this.artifacts().length !== 0) {
      this.selectedArtifactName = this.artifacts()[0].name ?? '';
      this.selectedArtifact = this.artifacts()[0].downloadUrl ?? '';
    }
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
    this.sanitizeDataBeforeFetching();
    this.productService
      .sendRequestToProductDetailVersionAPI(
        this.productId,
        this.isDevVersionsDisplayed(),
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
          this.selectedVersion.set(this.versions()[0]);
        }
      });
  }


  getVersionInDesigner(): void {
    if (this.versions().length === 0) {
      this.productService.sendRequestToGetProductVersionsForDesigner(this.productId
      ).subscribe(data => {
        const versionMap = data
          .map(dataVersionAndUrl => dataVersionAndUrl.version)
          .map(version => VERSION.displayPrefix.concat(version));
        data.forEach(data => {
          const currentVersion = 'Version '.concat(data.version);
          const versionAndUrl: ItemDropdown = { value: currentVersion, label: currentVersion, metaDataJsonUrl: data.url };
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
    this.selectedVersion.set('');
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
      .sendRequestToUpdateInstallationCount(this.productId)
      .subscribe((data: number) => this.installationCount.emit(data));
  }

  onUpdateInstallationCountForDesigner() {
    if (this.isDesignerEnvironment()) {
      this.onUpdateInstallationCount();
    }
  }

}
