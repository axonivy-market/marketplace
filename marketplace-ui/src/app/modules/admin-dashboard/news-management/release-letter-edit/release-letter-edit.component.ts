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
import { NgbAccordionModule } from '@ng-bootstrap/ng-bootstrap';

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
  selectedReleaseVersion: string = '';
  releaseLetter: ReleaseLetter = {
    releaseVersion: '',
    content: ''
  };
  isCreateMode = true;
  isSubmitting = signal<boolean>(false);
  errorMessage: string | null = null;

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const releaseVersionParam = params.get('release-version');
      if (releaseVersionParam) {
        this.isCreateMode = false;
        this.selectedReleaseVersion = releaseVersionParam;
        this.getReleaseLetter(this.selectedReleaseVersion);
      } else {
        this.isCreateMode = true;
      }
    });
  }

  getReleaseLetter(releaseVersion: string): void {
    this.adminDashboardService
      .getRelaseLetterByReleaseVersion(releaseVersion)
      .subscribe(response => {
        this.releaseLetter.content = response.content;
        this.releaseLetter.releaseVersion = response.releaseVersion;
      });
  }

  createReleaseLetter(releaseLetter: ReleaseLetter): void {
    this.adminDashboardService
      .createReleaseLetter(releaseLetter)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      // .subscribe(response => {
      //   this.releaseLetter.content = response.content;
      //   this.releaseLetter.releaseVersion = response.releaseVersion;
      // });
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
              'common.admin.releaseLetterEdit.releaseVersionAlreadyExistsErrorMessage'
            );
          }
        }
      });

    this.adminDashboardService
      .createReleaseLetter(releaseLetter)
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
              'common.admin.releaseLetterEdit.releaseVersionAlreadyExistsErrorMessage'
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
              'common.admin.releaseLetterEdit.releaseVersionAlreadyExistsErrorMessage'
            );
          }
        }
      });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    if (this.isSubmitting()) return;

    this.isSubmitting.set(true);

    this.updateReleaseLetter(this.selectedReleaseVersion, this.releaseLetter);
  }

  onClickingBackToNewsManagementButton(): void {
    this.router.navigate(['/internal-dashboard/news-management']);
  }

  onReleaseVersionChange() {
    this.errorMessage = null;
  }
}
