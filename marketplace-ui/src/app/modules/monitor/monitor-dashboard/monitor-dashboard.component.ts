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
import { ActivatedRoute, Router } from '@angular/router';
import { QUERY_PARAM_KEY } from '../../../shared/constants/query.params.constant';

@Component({
  selector: 'app-monitor-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    NgbTooltipModule,
    MonitoringRepoComponent,
  ],
  templateUrl: './monitor-dashboard.component.html',
  styleUrl: './monitor-dashboard.component.scss'
})
export class MonitoringDashboardComponent implements OnInit {
  readonly FOCUSED_TAB = FOCUSED_TAB;
  readonly STANDARD_TAB = STANDARD_TAB;

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
        if (params[QUERY_PARAM_KEY.ACTIVE_TAB]) {
          this.activeTab.set(params[QUERY_PARAM_KEY.ACTIVE_TAB]);
        }
        if (params[QUERY_PARAM_KEY.REPO_SEARCH]) {
          this.initialSearch.set(params[QUERY_PARAM_KEY.REPO_SEARCH]);
        }
      });
      this.pageTitleService.setTitleOnLangChange(
        'common.monitor.dashboard.pageTitle'
      );
    }
  }

  setActiveTab(tab: string): void {
    this.activeTab.set(tab);
    const queryParams = { activeTab: tab };
    this.router.navigate([], {
      relativeTo: this.route,
      queryParamsHandling: 'merge',
      queryParams
    });
  }
}
