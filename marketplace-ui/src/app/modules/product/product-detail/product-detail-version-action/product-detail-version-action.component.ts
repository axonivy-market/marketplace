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
  ViewChild,
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
import { ProductDetail } from '../../../../shared/models/product-detail.model';

@Component({
  selector: 'app-product-version-action',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    FormsModule,
    CommonDropdownComponent
  ],
  templateUrl: './product-detail-version-action.component.html',
  styleUrl: './product-detail-version-action.component.scss'
})
export class ProductDetailVersionActionComponent implements AfterViewInit {
  protected readonly environment = environment;
  @Output() installationCount = new EventEmitter<number>();
  @Input() productId!: string;
  @Input() actionType!: ProductDetailActionType;

  @ViewChild('artifactDownloadButton') artifactDownloadButton!: ElementRef;
  @ViewChild('artifactDownloadDialog') artifactDownloadDialog!: ElementRef;

  @Input() product!: ProductDetail;
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
  isDevVersionsDisplayed = signal(false);
  isDropDownDisplayed = signal(false);
  designerVersion = '';
  selectedArtifact: string | undefined = '';
  selectedArtifactName: string | undefined = '';
  versionMap: Map<string, ItemDropdown[]> = new Map();

  themeService = inject(ThemeService);
  productService = inject(ProductService);
  elementRef = inject(ElementRef);
  languageService = inject(LanguageService);
  routingQueryParamService = inject(RoutingQueryParamService);
  changeDetectorRef = inject(ChangeDetectorRef);

  ngAfterViewInit() {
    const tooltipTriggerList = [].slice.call(
      document.querySelectorAll('[data-bs-toggle="tooltip"]')
    );
    tooltipTriggerList.forEach(
      tooltipTriggerEl => new Tooltip(tooltipTriggerEl)
    );
  }

  onSelectVersion(version: string) {
    this.selectedVersion.set(version);
    this.artifacts.set(this.versionMap.get(this.selectedVersion()) ?? []);
    this.updateSelectedArtifact();
  }

  private updateSelectedArtifact() {
    this.artifacts().forEach(artifact => {
      if (artifact.name) {
        artifact.label = artifact.name;
      }
    });
      this.selectedArtifactName = this.artifacts()[0]?.name ?? '';
      this.selectedArtifact = this.artifacts()[0]?.downloadUrl ?? '';
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
    this.changeDetectorRef.detectChanges();
    this.reLocaleDialog();
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.reLocaleDialog();
  }

  reLocaleDialog() {
    const buttonPosition = this.getElementPosition(this.artifactDownloadButton);
    const dialogPosition = this.getElementPosition(this.artifactDownloadDialog);
    if (buttonPosition && dialogPosition) {
      const dialogElement = this.artifactDownloadDialog.nativeElement;

      dialogElement.style.position = 'absolute';
      dialogElement.style.top = `${buttonPosition.y + buttonPosition.height}px`;

      // Align the dialog to the center of the button
      const dialogWidth = dialogElement.offsetWidth;
      const buttonCenterX = buttonPosition.x + buttonPosition.width / 2;
      dialogElement.style.left = `${buttonCenterX - dialogWidth / 2}px`;
    }
  }

  getElementPosition(element: ElementRef) {
    if (element?.nativeElement) {
      const rect = element.nativeElement.getBoundingClientRect();
      return {
        x: rect.left + window.scrollX,
        y: rect.top + window.scrollY,
        width: rect.width,
        height: rect.height
      };
    }
    return null;
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
          this.artifacts.set(this.versionMap.get(this.selectedVersion()) ?? []);
          this.updateSelectedArtifact();
        }
      });
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
