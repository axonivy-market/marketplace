import {
  Component,
  inject,
  ViewEncapsulation,
  Signal,
  WritableSignal,
  computed,
  signal,
  OnInit,
  SecurityContext
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReleasePreviewService } from './release-preview.service';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';
import { LanguageService } from '../../core/services/language/language.service';
import { ThemeService } from '../../core/services/theme/theme.service';
import { CommonUtils } from '../../shared/utils/common.utils';
import { PRODUCT_DETAIL_TABS } from '../../shared/constants/common.constant';
import { ItemDropdown } from '../../shared/models/item-dropdown.model';
import { CommonDropdownComponent } from '../../shared/components/common-dropdown/common-dropdown.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SafeHtml, DomSanitizer } from '@angular/platform-browser';
import { DisplayValue } from '../../shared/models/display-value.model';
import { MultilingualismPipe } from '../../shared/pipes/multilingualism.pipe';
import { MarkdownService } from '../../shared/services/markdown.service';
import { PageTitleService } from '../../shared/services/page-title.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';

const DEFAULT_ACTIVE_TAB = 'description';
const MAX_FILE_SIZE_MB = 20;
@Component({
  selector: 'app-release-preview',
  imports: [
    CommonModule,
    FormsModule,
    CommonDropdownComponent,
    TranslateModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './release-preview.component.html',
  styleUrls: ['./release-preview.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class ReleasePreviewComponent implements OnInit {
  protected LoadingComponentId = LoadingComponentId;
  selectedFile: File | null = null;
  activeTab = DEFAULT_ACTIVE_TAB;
  selectedLanguage = 'en';
  isZipFile = false;
  isUploaded = false;
  shouldShowHint = false;
  isDragging = false;
  file: File | null = null;
  errorMessage: string | null = null;
  readmeContent: WritableSignal<ReleasePreviewData> = signal(
    {} as ReleasePreviewData
  );
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  detailTabs = PRODUCT_DETAIL_TABS;
  private scrollPositions: { [tabId: string]: number } = {};
  displayedTabsSignal: Signal<ItemDropdown[]> = computed(() => {
    this.languageService.selectedLanguage();
    return this.getDisplayedTabsSignal();
  });
  loadedReadmeContent: { [key: string]: SafeHtml } = {};

  private readonly sanitizer = inject(DomSanitizer);

  private readonly releasePreviewService = inject(ReleasePreviewService);
  private readonly markdownService = inject(MarkdownService);

  ngOnInit(): void {
    this.pageTitleService.setTitleOnLangChange('common.preview.pageTitle');
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;

    if (event.dataTransfer?.files.length) {
      const droppedFile = event.dataTransfer.files[0];
      this.setSelectedFile(droppedFile);
      this.onSubmit();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.setSelectedFile(input.files[0]);
      this.onSubmit();
    }
  }

  setSelectedFile(file: File) {
    const maxFileSize = MAX_FILE_SIZE_MB * 1024 * 1024;
    const isZip = file.type === 'application/zip' || file.name.toLowerCase().endsWith('.zip');
    const withinSize = file.size < maxFileSize;
    if (isZip && withinSize) {
      const replacing = !!this.selectedFile && this.selectedFile !== file;
      this.selectedFile = file;
      this.isZipFile = true;
      this.errorMessage = null;
      if (replacing) {
        this.isUploaded = false;
        this.readmeContent.set({} as ReleasePreviewData);
      }
    } else {
      this.selectedFile = null;
      this.isZipFile = false;

      if (!isZip) {
        this.errorMessage = 'common.preview.errors.invalidZip';
      } else if (!withinSize) {
        this.errorMessage = 'common.preview.errors.tooLarge';
      } else {
        this.errorMessage = 'common.preview.errors.seemsProblem';
      }
    }
  }

  fileSizeInMB(bytes: number): string {
    return (bytes / 1024 / 1024).toFixed(2);
  }

  removeFile() {
    this.selectedFile = null;
    this.isZipFile = false;
    this.isUploaded = false;
  }

  toggleHint() {
    this.shouldShowHint = !this.shouldShowHint;
  }

  onSubmit(): void {
    this.handlePreviewPage();
  }

  setActiveTab(event: string): void {
    this.activeTab = event;
    this.scrollPositions[this.activeTab] = window.scrollY;
  }

  handlePreviewPage(): void {
    if (!this.selectedFile || !this.isZipFile) {
      return;
    }

    this.releasePreviewService.extractZipDetails(this.selectedFile).subscribe({
      next: response => {
        this.readmeContent.set(response);
        this.renderReadmeContent();
        this.isUploaded = true;
        this.shouldShowHint = false;
      },
      error: (err) => {
        this.isUploaded = true;
        this.errorMessage = err.error?.message;
    }
    });
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

  getContent(value: string): boolean {
    const content = this.readmeContent();

    const tabContent = content?.[value as keyof ReleasePreviewData];
    return (
      !!tabContent &&
      Object.keys(tabContent).length > 0 &&
      CommonUtils.isContentDisplayedBasedOnLanguage(
        tabContent,
        this.languageService.selectedLanguage()
      )
    );
  }

  getSelectedTabLabel() {
    return CommonUtils.getLabel(this.activeTab, PRODUCT_DETAIL_TABS);
  }

  getReadmeContentValue(key: ItemDropdown): DisplayValue | null {
    type tabName = 'description' | 'demo' | 'setup';
    const value = key.value as tabName;
    return this.readmeContent()?.[value] ?? null;
  }

  private renderReadmeContent(): void {
    for (const tab of this.detailTabs) {
      const contentValue = this.getReadmeContentValue(tab);
      if (!contentValue) continue;

      const translatedContent =
        new MultilingualismPipe().transform(
          contentValue,
          this.languageService.selectedLanguage()
        ) || '';

      const renderedHtml =
        this.markdownService.parseMarkdown(translatedContent);

      const sanitizedHtml = this.sanitizer.sanitize(
        SecurityContext.HTML,
        renderedHtml
      );

      this.loadedReadmeContent[tab.value] = sanitizedHtml ?? '';
    }
  }
}
