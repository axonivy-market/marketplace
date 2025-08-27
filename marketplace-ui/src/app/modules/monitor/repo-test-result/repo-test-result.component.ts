import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { IsEmptyObjectPipe } from '../../../shared/pipes/is-empty-object.pipe';
import { BuildStatusEntriesPipe } from "../../../shared/pipes/build-status-entries.pipe";
import { Repository } from '../github.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-repo-test-result',
  standalone: true,
  imports: [CommonModule, TranslateModule, IsEmptyObjectPipe, BuildStatusEntriesPipe],
  templateUrl: './repo-test-result.component.html',
  styleUrl: './repo-test-result.component.scss'
})
export class RepoTestResultComponent {
  @Input() workflow!: string;
  @Input() conclusion!: string;
  @Input() isReportMode = false;
  @Input() repository!: Repository;
  @Input() mode!: 'default' | 'report';
  badges: Record<string, { src: string; alt: string }> = {
    success: { src: '/assets/images/misc/pass-badge.svg', alt: 'Pass badge' },
    failure: { src: '/assets/images/misc/fail-badge.svg', alt: 'Fail badge' }
  };


  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  router = inject(Router);

  onBadgeClick(repo: string, workflow: string) {
    const upperWorkflow = workflow.toUpperCase();
    this.router.navigate(['/monitoring', repo, upperWorkflow]);
  }
}
