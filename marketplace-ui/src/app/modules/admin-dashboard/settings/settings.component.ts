import { Component, DestroyRef, inject, OnInit } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { AppSetting, AppSettingsService } from "./settings.component.service";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { TranslateModule } from "@ngx-translate/core";
import { LoadingSpinnerComponent } from "../../../shared/components/loading-spinner/loading-spinner.component";
import { LanguageService } from "../../../core/services/language/language.service";
import { PageTitleService } from "../../../shared/services/page-title.service";
import { debounceTime, Subject } from "rxjs";
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';
import { ASCENDING, DESCENDING } from "../../../shared/constants/common.constant";

const SHOW_ALL_PAGE_SIZE = -1;
const DEBOUNCE_TIME = 300;

const CATEGORY_CLASS_MAP: Record<string, string> = {
  SCHEDULING: 'badge-scheduling',
  GITHUB: 'badge-github',
  MATOMO: 'badge-matomo',
  MAIL: 'badge-mail',
  SECURITY: 'badge-security',
  GENERAL: 'badge-general'
};

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    LoadingSpinnerComponent,
    NgbPaginationModule
  ],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class AdminSettingsComponent implements OnInit {

  protected readonly LoadingComponentId = LoadingComponentId;

  private readonly appSettingsService = inject(AppSettingsService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly languageService = inject(LanguageService);
  private readonly pageTitleService = inject(PageTitleService);

  private settings: AppSetting[] = [];
  protected filteredSettings: AppSetting[] = [];
  protected sortColumn: keyof AppSetting = 'category';
  protected sortDirection = ASCENDING;

  protected page = 1;
  protected pageSize = 10;
  protected searchText = '';
  protected readonly visibleSecrets = new Set<string>();
  protected isSaving = false;

  private readonly searchChanged = new Subject<string>();

  constructor() {
    this.searchChanged
      .pipe(debounceTime(DEBOUNCE_TIME), takeUntilDestroyed())
      .subscribe(() => this.filterSettings());
  }

  ngOnInit(): void {
    this.pageTitleService.setTitleOnLangChange('common.admin.settings.title');
    this.loadSettings();
  }

  protected get pagedSettings(): AppSetting[] {
    if (this.pageSize === SHOW_ALL_PAGE_SIZE) {
      return this.filteredSettings;
    }
    const start = (this.page - 1) * this.pageSize;
    return this.filteredSettings.slice(start, start + this.pageSize);
  }

  protected get totalElements(): number {
    return this.filteredSettings.length;
  }

  protected onPageChange(page: number): void {
    this.page = page;
  }

  private loadSettings(): void {
    this.appSettingsService
      .getSettings()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: settings => {
          this.settings = settings;
          this.filteredSettings = [...settings];
          this.applySorting();
         },
         error: () => {
           this.settings = [];
           this.filteredSettings = [];
         }
      });
  }

  protected onSearchChanged(value: string): void {
    this.searchText = value;
    this.searchChanged.next(value);
    this.page = 1;
  }

  protected onClearSearch(): void {
    this.searchText = '';
    this.filterSettings();
    this.page = 1;
  }

  private filterSettings(): void {
    const keyword = this.searchText.toLowerCase();
    this.filteredSettings = this.settings.filter(
      s =>
        s.settingKey.toLowerCase().includes(keyword) ||
        s.category.toLowerCase().includes(keyword) ||
        s.description?.toLowerCase().includes(keyword)
    );
    this.applySorting();
  }

  protected save(setting: AppSetting): void {
    const previousValue = this.settings.find(
      s => s.settingKey === setting.settingKey
    )?.settingValue ?? setting.settingValue;

    this.isSaving = true;
    this.appSettingsService.updateSetting(setting).subscribe({
      error: () => {
        setting.settingValue = previousValue;
        this.isSaving = false;
      },
      complete: () => {
        this.isSaving = false;
      }
    });
  }

  protected sortBy(column: keyof AppSetting): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === ASCENDING ? DESCENDING : ASCENDING;
    } else {
      this.sortColumn = column;
      this.sortDirection = ASCENDING;
    }
    this.applySorting();
  }

  private applySorting(): void {
    this.filteredSettings.sort((a, b) => {
      const valueA = String(a[this.sortColumn] ?? '').toLowerCase();
      const valueB = String(b[this.sortColumn] ?? '').toLowerCase();
      const result = valueA.localeCompare(valueB);
      return this.sortDirection === ASCENDING ? result : -result;
    });
  }

  protected toggleSecret(settingKey: string): void {
    if (this.visibleSecrets.has(settingKey)) {
      this.visibleSecrets.delete(settingKey);
    } else {
      this.visibleSecrets.add(settingKey);
    }
  }

  protected isSecretVisible(settingKey: string): boolean {
    return this.visibleSecrets.has(settingKey);
  }

  protected getSortIcon(column: keyof AppSetting): string {
    if (this.sortColumn !== column) { return 'ti-arrows-sort'; }
    return this.sortDirection === ASCENDING ? 'ti-arrow-up' : 'ti-arrow-down';
  }

  protected getCategoryClass(category: string): string {
    return CATEGORY_CLASS_MAP[category] ?? 'bg-light text-dark';
  }
}