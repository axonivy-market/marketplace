import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { IsEmptyObjectPipe } from '../../../shared/pipes/is-empty-object.pipe';
import { BuildStatusEntriesPipe } from "../../../shared/pipes/build-status-entries.pipe";
import { TestResult } from '../github.service';
import { LanguageService } from '../../../core/services/language/language.service';

@Component({
  selector: 'app-repo-test-result',
  standalone: true,
  imports: [CommonModule, TranslateModule, IsEmptyObjectPipe, BuildStatusEntriesPipe],
  templateUrl: './repo-test-result.component.html',
  styleUrl: './repo-test-result.component.scss'
})
export class RepoTestResultComponent {
  @Input() workflow!: string;
  @Input() conclusion!: string | null;
  @Input() lastUpdated!: string | Date;
  @Input() testResults: TestResult[] = [];
  @Input() isReportMode = false;

  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
}
