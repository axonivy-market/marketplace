import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
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
  readonly DEFAULT = DEFAULT_MODE;
  readonly REPORT = REPORT_MODE;
  @Input() workflowType = '';
  @Input() workflowInfo?: WorkflowInformation;
  @Input() repository!: Repository;
  @Input() mode!: typeof DEFAULT_MODE | typeof REPORT_MODE;
  badges: Record<string, { src: string; alt: string }> = {
    success: { src: '/assets/images/misc/pass-badge.svg', alt: 'Pass badge' },
    failure: { src: '/assets/images/misc/fail-badge.svg', alt: 'Fail badge' }
  };

  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  router = inject(Router);

  getConclusionKey(conclusion?: string): string {
    return (conclusion ?? '').toLowerCase();
  }

  onBadgeClick(repo: string, workflowType: string, mode: string) {
    if (mode === REPORT_MODE) {
      this.router.navigate(['/monitoring', repo, workflowType]);
    } else {
      const lastBuiltUrl = this.workflowInfo?.lastBuiltRunUrl;
      if (lastBuiltUrl) {
        window.open(lastBuiltUrl, '_blank');
      }
    }
  }

  getWorkflowDisplay(state: string): {
    icon: string;
    label: string;
    tooltip: string;
  } {
    if (!state) {
      return { icon: '', label: '', tooltip: '' };
    }

    let icon = '🟢';
    if (state.includes('disabled')) {
      icon = '⚠️';
    } else if (state === 'deleted') {
      icon = '❌';
    }

    const labelKey = 'common.monitor.workflow.status.' + state;
    const tooltipKey = 'common.monitor.workflow.tooltip.' + state;

    return {
      icon,
      label: this.translateService.instant(labelKey),
      tooltip: this.translateService.instant(tooltipKey)
    };
  }
}
