import {
  Component,
  inject,
  ViewEncapsulation,
  Signal,
  WritableSignal,
  computed,
  signal
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
import { MarkdownModule, MarkdownService } from 'ngx-markdown';
import { DisplayValue } from '../../shared/models/display-value.model';
import { MultilingualismPipe } from '../../shared/pipes/multilingualism.pipe';

const DEFAULT_ACTIVE_TAB = 'description';
@Component({
  selector: 'app-release-preview',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CommonDropdownComponent,
    TranslateModule,
    MarkdownModule,
    MultilingualismPipe
  ],
  templateUrl: './release-preview.component.html',
  styleUrls: ['./release-preview.component.scss'],
  providers: [MarkdownService],
  encapsulation: ViewEncapsulation.Emulated
})
export class ReleasePreviewComponent {
  selectedFile: File | null = null;
  activeTab = DEFAULT_ACTIVE_TAB;
  selectedLanguage = 'en';
  isZipFile = false;
  readmeContent: WritableSignal<ReleasePreviewData> = signal(
    {} as ReleasePreviewData
  );
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  detailTabs = PRODUCT_DETAIL_TABS;
  displayedTabsSignal: Signal<ItemDropdown[]> = computed(() => {
    this.languageService.selectedLanguage();
    return this.getDisplayedTabsSignal();
  });

  private readonly releasePreviewService = inject(ReleasePreviewService);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.selectedFile = file;

      // Check if the selected file is a ZIP file
      this.isZipFile =
        file.type === 'application/zip' || file.name.endsWith('.zip');
    }
  }

  onSubmit(): void {
    this.handlePreviewPage();
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  onTabChange(event: string) {
    this.setActiveTab(event);
  }

  handlePreviewPage(): void {
    if (!this.selectedFile || !this.isZipFile) {
      return;
    }

    this.releasePreviewService.extractZipDetails(this.selectedFile).subscribe({
      next: response => {
        this.readmeContent.set(response);
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
        )
    };

    return conditions[value] ?? false;
  }

  getSelectedTabLabel() {
    return CommonUtils.getLabel(this.activeTab, PRODUCT_DETAIL_TABS);
  }

  getReadmeContentValue(key: ItemDropdown): DisplayValue | null {
    type tabName = 'description' | 'demo' | 'setup';
    const value = key.value as tabName;
    return this.readmeContent()[value];
  }
}
