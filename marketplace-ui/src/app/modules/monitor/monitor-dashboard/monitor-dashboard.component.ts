import { Component, computed, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BuildStatusEntriesPipe } from "../../../shared/pipes/build-status-entries.pipe";
import { IsEmptyObjectPipe } from '../../../shared/pipes/is-empty-object.pipe';
import { BuildBadgeTooltipComponent } from '../build-badge-tooltip/build-badge-tooltip.component';
import {
  MONITORING_WIKI_LINK
} from '../../../shared/constants/common.constant';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { MonitoringRepoComponent } from "../monitor-repo/monitor-repo.component";

@Component({
  selector: 'app-monitor-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    BuildStatusEntriesPipe,
    IsEmptyObjectPipe,
    BuildBadgeTooltipComponent,
    NgbTooltipModule,
    MonitoringRepoComponent
],
  templateUrl: './monitor-dashboard.component.html',
  styleUrl: './monitor-dashboard.component.scss'
})
export class MonitoringDashboardComponent implements OnInit {
  repositories = signal<Repository[]>([]);
  focusedRepo = computed(() => this.repositories().filter(r => r.focused));
  standardRepo = computed(() => this.repositories().filter(r => !r.focused));
  loading = true;
  error = '';
  isReloading = false;
  languageService = inject(LanguageService);
  githubService = inject(GithubService);
  translateService = inject(TranslateService);
  themeService = inject(ThemeService);
  router = inject(Router);
  pageTitleService: PageTitleService = inject(PageTitleService);
  platformId = inject(PLATFORM_ID);

  monitoringWikiLink = MONITORING_WIKI_LINK;
  activeTab = 'focused';
  isLoading = false;

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
        this.repositories.set(data);
        this.isLoading = false;
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
