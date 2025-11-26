import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  FOCUSED_TAB,
  MONITORING_WIKI_LINK,
  STANDARD_TAB
} from '../../../shared/constants/common.constant';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { MonitoringRepoComponent } from '../monitor-repo/monitor-repo.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-monitor-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    NgbTooltipModule,
    MonitoringRepoComponent,
    LoadingSpinnerComponent
  ],
  templateUrl: './monitor-dashboard.component.html',
  styleUrl: './monitor-dashboard.component.scss'
})
export class MonitoringDashboardComponent implements OnInit {
  readonly FOCUSED_TAB = FOCUSED_TAB;
  readonly STANDARD_TAB = STANDARD_TAB;

  protected LoadingComponentId = LoadingComponentId;
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  themeService = inject(ThemeService);
  pageTitleService: PageTitleService = inject(PageTitleService);
  platformId = inject(PLATFORM_ID);
  route = inject(ActivatedRoute);
  router = inject(Router);
  error = '';
  monitoringWikiLink = MONITORING_WIKI_LINK;
  activeTab = signal<string>(FOCUSED_TAB);
  initialSearch = signal<string>('');

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.route.queryParams.subscribe(params => {
        if (params['isFocused']) {
          let focusedTab;
          if (params['isFocused'] === 'true') {
            focusedTab = FOCUSED_TAB;
          } else {
            focusedTab = STANDARD_TAB;
          }
          this.activeTab.set(focusedTab);
        }
        if (params['repoSearch']) {
          this.initialSearch.set(params['repoSearch']);
        }
      });
      this.pageTitleService.setTitleOnLangChange(
        'common.monitor.dashboard.pageTitle'
      );
    }
  }

  setActiveTab(tab: string): void {
    this.activeTab.set(tab);
    let isFocusedProduct;
    if (tab === FOCUSED_TAB) {
      isFocusedProduct = 'true';
    } else {
      isFocusedProduct = 'false';
    }
    const queryParams = { isFocused: isFocusedProduct };
    this.router.navigate([], {
      relativeTo: this.route,
      queryParamsHandling: 'merge',
      queryParams
    });
  }
}
