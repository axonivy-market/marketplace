import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Component, inject, Input, OnInit } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { IsEmptyObjectPipe } from '../../../shared/pipes/is-empty-object.pipe';
import { BuildStatusEntriesPipe } from "../../../shared/pipes/build-status-entries.pipe";
import { Repository, WorkflowInformation } from '../github.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { Router } from '@angular/router';
import { DEFAULT_MODE, REPORT_MODE } from '../../../shared/constants/common.constant';

@Component({
  selector: 'app-repo-test-result',
  standalone: true,
  imports: [CommonModule, TranslateModule, IsEmptyObjectPipe, BuildStatusEntriesPipe, NgOptimizedImage],
  templateUrl: './repo-test-result.component.html',
  styleUrl: './repo-test-result.component.scss'
})
export class RepoTestResultComponent {
  @Input() workflowType: string = '';
  @Input() workflowInfo?: WorkflowInformation;
  @Input() repository!: Repository;
  @Input() mode!: 'default' | 'report';
  badges: Record<string, { src: string; alt: string }> = {
    success: { src: '/assets/images/misc/pass-badge.svg', alt: 'Pass badge' },
    failure: { src: '/assets/images/misc/fail-badge.svg', alt: 'Fail badge' }
  };

  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  router = inject(Router);

  getConclusionKey(conclusion?: string): string {
    return (conclusion || '').toLowerCase();
  }

  onBadgeClick(repo: string, workflowType: string, mode: string) {
    if (mode == REPORT_MODE) {
      this.router.navigate(['/monitoring', repo, workflowType]);
    } else {
      const lastBuiltUrl = this.workflowInfo?.lastBuiltRun;
      if (lastBuiltUrl) {
        window.open(lastBuiltUrl, '_blank');
      }
    }
  }
}
