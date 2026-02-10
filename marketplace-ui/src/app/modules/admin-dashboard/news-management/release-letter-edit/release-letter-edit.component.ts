import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { MarkdownEditorComponent } from '../../../../shared/components/markdown-editor/markdown-editor.component';
import { RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED } from '../../../../shared/constants/common.constant';
import { ReleaseLetter } from '../../../../shared/models/release-letter-request.model';
import { PageTitleService } from '../../../../shared/services/page-title.service';
import { AdminDashboardService } from '../../admin-dashboard.service';

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
  selectedSprint: string = '';
  releaseLetter: ReleaseLetter = {
    sprint: '',
    content: '',
    active: false
  };
  isCreateMode = true;
  isSubmitting = signal<boolean>(false);
  errorMessage: string | null = null;

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const sprintParam = params.get('sprint');
      if (sprintParam) {
        this.isCreateMode = false;
        this.selectedSprint = sprintParam;
        this.getRelaseLetterBySprint(this.selectedSprint);
      } else {
        this.isCreateMode = true;
      }
    });
  }

  getRelaseLetterBySprint(sprint: string): void {
    this.adminDashboardService
      .getRelaseLetterBySprint(sprint)
      .subscribe(response => {
        this.releaseLetter.content = response.content;
        this.releaseLetter.sprint = response.sprint;
        this.releaseLetter.active = response.active;
      });
  }

  createReleaseLetter(releaseLetter: ReleaseLetter): void {
    this.adminDashboardService
      .createReleaseLetter(releaseLetter)
      .pipe(
        finalize(() => {
          this.isSubmitting.set(false);
        })
      )
      .subscribe({
        next: _res => {
          this.router.navigate(['/internal-dashboard/news-management']);
        },
        error: err => {
          if (
            RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString() ===
            err.error.helpCode
          ) {
            this.errorMessage = this.translateService.instant(
              'common.admin.releaseLetterEdit.sprintAlreadyExistsErrorMessage'
            );
          }
        }
      });
  }

  updateReleaseLetter(
    releaseVersion: string,
    releaseLetter: ReleaseLetter
  ): void {
    this.adminDashboardService
      .updateReleaseLetter(releaseVersion, releaseLetter)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: _res => {
          this.router.navigate(['/internal-dashboard/news-management']);
        },
        error: err => {
          if (
            RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString() ===
            err.error.helpCode
          ) {
            this.errorMessage = this.translateService.instant(
              'common.admin.releaseLetterEdit.sprintAlreadyExistsErrorMessage'
            );
          }
        }
      });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    if (this.isSubmitting()) return;

    this.isSubmitting.set(true);

    if (this.isCreateMode) {
      this.createReleaseLetter(this.releaseLetter);
      return;
    }

    this.updateReleaseLetter(this.selectedSprint, this.releaseLetter);
  }

  onClickingBackToNewsManagementButton(): void {
    this.router.navigate(['/internal-dashboard/news-management']);
  }

  onReleaseVersionChange() {
    this.errorMessage = null;
  }
}
