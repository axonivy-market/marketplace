import { Component, inject, OnDestroy, OnInit } from "@angular/core";
import { AppSetting, AppSettingsService } from "./settings.component.service";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { LoadingSpinnerComponent } from "../../../shared/components/loading-spinner/loading-spinner.component";
import { LanguageService } from "../../../core/services/language/language.service";
import { PageTitleService } from "../../../shared/services/page-title.service";
import { debounceTime, finalize, Subject, Subscription } from "rxjs";
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';

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
export class AdminSettingsComponent implements OnInit, OnDestroy {

  protected readonly LoadingComponentId = LoadingComponentId;

  appSettingsService = inject(AppSettingsService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  pageTitleService = inject(PageTitleService);

  settings: AppSetting[] = [];
  filteredSettings: AppSetting[] = [];
  sortColumn: keyof AppSetting = 'category';
  sortDirection: 'asc' | 'desc' = 'asc';

  page = 1;
  pageSize = 10;
  readonly ALL_ITEMS_PAGE_SIZE = -1;

  searchText = '';
  isLoading = false;
  protected visibleSecrets = new Set<string>();

  private readonly searchChanged = new Subject<string>();
  private readonly subscriptions: Subscription[] = [];

  ngOnInit(): void {

    this.pageTitleService.setTitleOnLangChange(
      'common.admin.settings.title'
    );

    const searchSubscription = this.searchChanged
      .pipe(debounceTime(300))
      .subscribe(() => {
        this.filterSettings();
      });

    this.subscriptions.push(searchSubscription);

    this.loadSettings();
  }

get pagedSettings(): AppSetting[] {
  if (this.pageSize === this.ALL_ITEMS_PAGE_SIZE) {
    return this.filteredSettings;
  }

  const start = (this.page - 1) * this.pageSize;
  return this.filteredSettings.slice(start, start + this.pageSize);
}

get totalElements(): number {
  return this.filteredSettings.length;
}

onPageChange(page: number): void {
  this.page = page;
}

onPageSizeChanged(pageSize: number): void {
  this.pageSize = pageSize;
  this.page = 1;
}

  loadSettings(): void {
    this.isLoading = true;
    const subscription = this.appSettingsService
      .getSettings('')
      .pipe(
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe(settings => {
        this.settings = settings;
        this.filteredSettings = [...settings];
      });

    this.subscriptions.push(subscription);
  }

  onSearchChanged(value: string): void {
    this.searchText = value;
    this.searchChanged.next(value);
    this.page = 1;
  }

  onClearSearch(): void {
    this.searchText = '';
    this.filterSettings();
    this.page = 1;
  }

  private filterSettings(): void {

    const keyword = this.searchText.toLowerCase();

    this.filteredSettings = this.settings.filter(
      setting =>
        setting.settingKey.toLowerCase().includes(keyword)
        || setting.category.toLowerCase().includes(keyword)
        || setting.description?.toLowerCase().includes(keyword)
    );
  }

  save(setting: AppSetting): void {

    this.appSettingsService
      .updateSetting(setting)
      .subscribe();
  }

  sortBy(column: keyof AppSetting): void {
    if (this.sortColumn === column) {
      this.sortDirection =
        this.sortDirection === 'asc'
          ? 'desc'
          : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }

    this.applySorting();
  }

  private applySorting(): void {
    this.filteredSettings.sort((a, b) => {
      const valueA = String(a[this.sortColumn] ?? '').toLowerCase();
      const valueB = String(b[this.sortColumn] ?? '').toLowerCase();

      const result = valueA.localeCompare(valueB);

      return this.sortDirection === 'asc'
        ? result
        : -result;
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

  getCategoryClass(category: string): string {
  switch (category) {
    case 'SCHEDULING':
      return 'bg-primary';

    case 'GITHUB':
      return 'bg-dark';

    case 'MATOMO':
      return 'bg-info';

    case 'MAIL':
      return 'bg-success';

    case 'SECURITY':
      return 'bg-danger';

    case 'CORS':
      return 'bg-warning text-dark';

    case 'APPLICATION':
      return 'bg-secondary';

    default:
      return 'bg-light text-dark';
  }
}

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription =>
      subscription.unsubscribe()
    );
  }
}