import { Component, inject, OnDestroy, OnInit } from "@angular/core";
import { AppSetting, AppSettingsService } from "./app-settings.component.service";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { LoadingSpinnerComponent } from "../../../shared/components/loading-spinner/loading-spinner.component";
import { LanguageService } from "../../../core/services/language/language.service";
import { PageTitleService } from "../../../shared/services/page-title.service";
import { debounceTime, finalize, Subject, Subscription } from "rxjs";
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './app-settings.component.html',
  styleUrls: ['./app-settings.component.scss']
})
export class AdminSettingsComponent implements OnInit, OnDestroy {

  protected readonly LoadingComponentId = LoadingComponentId;

  appSettingsService = inject(AppSettingsService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  pageTitleService = inject(PageTitleService);

  settings: AppSetting[] = [];
  filteredSettings: AppSetting[] = [];

  searchText = '';
  isLoading = false;

  private readonly searchChanged = new Subject<string>();
  private readonly subscriptions: Subscription[] = [];

  ngOnInit(): void {

    this.pageTitleService.setTitleOnLangChange(
      'common.admin.app-settings.title'
    );

    const searchSubscription = this.searchChanged
      .pipe(debounceTime(300))
      .subscribe(() => {
        this.filterSettings();
      });

    this.subscriptions.push(searchSubscription);

    this.loadSettings();
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
  }

  onClearSearch(): void {
    this.searchText = '';
    this.filterSettings();
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

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription =>
      subscription.unsubscribe()
    );
  }
}