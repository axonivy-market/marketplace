import { CommonModule } from '@angular/common';
import { Component, inject, input, Signal, signal, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MarkdownEditorComponent } from '../../../../shared/components/markdown-editor/markdown-editor.component';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { PageTitleService } from '../../../../shared/services/page-title.service';
import { AdminDashboardService } from '../../admin-dashboard.service';
import { ReleaseLetter } from '../../../../shared/models/release-letter-request.model';
import { finalize } from 'rxjs';
import { RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED } from '../../../../shared/constants/common.constant';

@Component({
  selector: 'app-release-letter-edit',
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    MarkdownEditorComponent
  ],
  templateUrl: './release-letter-edit.component.html',
  styleUrl: './release-letter-edit.component.scss'
})
export class ReleaseLetterEditComponent {
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  adminDashboardService = inject(AdminDashboardService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  easyMDE!: EasyMDE;
  releaseLetterValue = '';
  releaseVersion = '';
  selectedReleaseVersion = '';
  releaseLetter: ReleaseLetter = {
    releaseVersion: '',
    content: ''
  };
  isSubmitting = signal<boolean>(false);
  errorMessage: string | null = null;

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.selectedReleaseVersion = params.get('release-version') ?? '';
      this.getReleaseLetter();
    });
  }

  getReleaseLetter(): void {
    this.adminDashboardService
      .getRelaseLetterByReleaseVersion(this.selectedReleaseVersion)
      .subscribe(response => {
        this.releaseLetterValue = response.content;
        this.releaseLetter.content = response.content;
        this.releaseLetter.releaseVersion = response.releaseVersion;
      });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    if (this.isSubmitting()) return;
    
    this.isSubmitting.set(true);
    this.adminDashboardService
      .updateReleaseLetter(this.selectedReleaseVersion, this.releaseLetter)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: _res => {
          this.router.navigate(['/internal-dashboard/news-management']);
        },
        error: err => {
          if (RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString() === err.error.helpCode) {
            this.errorMessage = "A letter of this release version already exists. Please choose a different release version.";
          }
        }
      });
  }

  onClickingBackToNewsManagementButton(): void {
    this.router.navigate(['/internal-dashboard/news-management']);
  }

  onReleaseVersionChange() {
    this.errorMessage = null;
  }
}
