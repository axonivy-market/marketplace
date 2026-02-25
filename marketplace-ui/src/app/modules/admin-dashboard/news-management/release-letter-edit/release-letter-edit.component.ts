import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  Inject,
  inject,
  OnInit,
  PLATFORM_ID,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { MarkdownEditorComponent } from '../../../../shared/components/markdown-editor/markdown-editor.component';
import {
  RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED,
  SPRINT_CANNOT_BE_BLANK
} from '../../../../shared/constants/common.constant';
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
export class ReleaseLetterEditComponent implements OnInit {
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  adminDashboardService = inject(AdminDashboardService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  easyMDE!: EasyMDE;
  selectedSprint = '';
  releaseLetter: ReleaseLetter = {
    sprint: '',
    content: '',
    createdAt: '',
    latest: false
  };
  isCreateMode = true;
  isSubmitting = signal<boolean>(false);
  sprintErrorMessage: string | null = null;
  genericErrorMessage: string | null = null;
  isBrowser: boolean;
  newsManangementUrl = '/internal-dashboard/news-management';

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit() {
    if (this.isBrowser) {
      this.pageTitleService.setTitleOnLangChange(
        'common.admin.newsManagement.pageTitle'
      );
    }
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
        this.releaseLetter.latest = response.latest;
      });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    if (this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);

    if (this.isCreateMode) {
      this.createReleaseLetter(this.releaseLetter);
      return;
    }

    this.updateReleaseLetter(this.selectedSprint, this.releaseLetter);
  }

  createReleaseLetter(releaseLetter: ReleaseLetter) {
    this.adminDashboardService
      .createReleaseLetter(releaseLetter)
      .pipe(
        finalize(() => {
          this.isSubmitting.set(false);
        })
      )
      .subscribe({
        next: _res => {
          this.router.navigate([this.newsManangementUrl]);
        },
        error: err => {
          this.handleError(err.error.helpCode);
        }
      });
  }
  updateReleaseLetter(releaseVersion: string, releaseLetter: ReleaseLetter) {
    this.adminDashboardService
      .updateReleaseLetter(releaseVersion, releaseLetter)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: _res => {
          this.router.navigate([this.newsManangementUrl]);
        },
        error: err => {
          this.handleError(err.error.helpCode);
        }
      });
  }

  handleError(errorHelpcode: string) {
    switch (errorHelpcode) {
      case SPRINT_CANNOT_BE_BLANK.toString():
        this.sprintErrorMessage = this.translateService.instant(
          'common.admin.releaseLetterEdit.sprintCannotBeBlankErrorMessage'
        );
        return;

      case RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.toString():
        this.sprintErrorMessage = this.translateService.instant(
          'common.admin.releaseLetterEdit.sprintAlreadyExistsErrorMessage'
        );
        return;

      default:
        this.genericErrorMessage = this.translateService.instant(
          'common.admin.releaseLetterEdit.genericErrorMessage'
        );
        return;
    }
  }

  onClickingBackToNewsManagementButton(): void {
    this.router.navigate([this.newsManangementUrl]);
  }

  onReleaseVersionChange() {
    this.sprintErrorMessage = null;
  }
}
