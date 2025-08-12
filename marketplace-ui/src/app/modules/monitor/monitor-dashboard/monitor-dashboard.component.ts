import { Component, computed, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { GithubService, Repository } from '../github.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule } from '@ngx-translate/core';
import { BuildStatusEntriesPipe } from "../../../shared/pipes/build-status-entries.pipe";
import { WorkflowIconPipe } from "../../../shared/pipes/workflow-icon.pipe";
import { IsEmptyObjectPipe } from '../../../shared/pipes/is-empty-object.pipe';
import { BuildBadgeTooltipComponent } from '../build-badge-tooltip/build-badge-tooltip.component';
import {
  CI_BUILD,
  DEV_BUILD,
  MONITORING_WIKI_LINK
} from '../../../shared/constants/common.constant';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { PageTitleService } from '../../../shared/services/page-title.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    BuildStatusEntriesPipe,
    WorkflowIconPipe,
    IsEmptyObjectPipe,
    BuildBadgeTooltipComponent,
    NgbTooltipModule
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
  router = inject(Router);
  platformId = inject(PLATFORM_ID);

  pageTitleService: PageTitleService = inject(PageTitleService);
  ciBuild = CI_BUILD;
  devBuild = DEV_BUILD;
  monitoringWikiLink = MONITORING_WIKI_LINK;

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadRepositories();
      this.pageTitleService.setTitleOnLangChange(
        'common.monitor.dashboard.pageTitle'
      );
    } else {
      this.loading = false;
    }
  }

  loadRepositories(): void {
    this.loading = true;
    this.githubService.getRepositories().subscribe({
      next: data => {
        this.repositories.set(data);
        this.loading = false;
      },
      error: err => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  onBadgeClick(repo: string, workflow: string) {
    const upperWorkflow = workflow.toUpperCase();
    this.router.navigate(['/report', repo, upperWorkflow]);
  }
}
