import { Component, computed, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FOCUSED_TAB, MONITORING_WIKI_LINK, STANDARD_TAB } from '../../../shared/constants/common.constant';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { MonitoringRepoComponent } from "../monitor-repo/monitor-repo.component";
import { LoadingSpinnerComponent } from "../../../shared/components/loading-spinner/loading-spinner.component";
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';

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
  githubService = inject(GithubService);
  translateService = inject(TranslateService);
  themeService = inject(ThemeService);
  pageTitleService: PageTitleService = inject(PageTitleService);
  platformId = inject(PLATFORM_ID);

  error = '';
  monitoringWikiLink = MONITORING_WIKI_LINK;
  activeTab = FOCUSED_TAB;
  isLoading = false;
  repositories = signal<Repository[]>([]);
  focusedRepo = computed(() => this.repositories().filter(r => r.focused));
  standardRepo = computed(() => this.repositories().filter(r => !r.focused));

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadRepositories();
      this.pageTitleService.setTitleOnLangChange(
        'common.monitor.dashboard.pageTitle'
      );
    } else {
      this.isLoading = false;
    }
  }

  loadRepositories(): void {
    this.isLoading = true;
    this.githubService.getRepositories().subscribe({
      next: data => {
        data.sort((repo1, repo2) => repo1.repoName.localeCompare(repo2.repoName));
        this.repositories.set(data);
        this.isLoading = false;
        this.error = '';
      },
      error: err => {
        this.error = err.message;
        this.isLoading = false;
      }
    });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
