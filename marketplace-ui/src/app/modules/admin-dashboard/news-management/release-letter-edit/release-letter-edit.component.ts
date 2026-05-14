import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, computed, DestroyRef, Inject, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { finalize, switchMap } from 'rxjs';
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
import { AppModalService } from '../../../../shared/services/app-modal.service';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { LoadingComponentId } from '../../../../shared/enums/loading-component-id';
import { NewsManagementService } from '../news-management.service';

@Component({
  selector: 'app-release-letter-edit',
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule, MarkdownEditorComponent, LoadingSpinnerComponent],
  templateUrl: './release-letter-edit.component.html',
  styleUrl: './release-letter-edit.component.scss'
})
export class ReleaseLetterEditComponent implements OnInit {
  protected LoadingComponentId = LoadingComponentId;
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  adminDashboardService = inject(AdminDashboardService);
  newsManagementService = inject(NewsManagementService);
  appModalService = inject(AppModalService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  destroyRef = inject(DestroyRef);
  easyMDE!: EasyMDE;
  selectedId = '';
  selectedSprint = '';
  releaseLetter: ReleaseLetter = {
    id: '',
    sprint: '',
    content: '',
    createdAt: '',
    updatedAt: '',
    latest: false,
    draftContent: ''
  };
  isCreateMode = true;
  isSubmitting = signal<boolean>(false);
  isSavingAsDraft = signal<boolean>(false);
  isInitializing = signal<boolean>(false);
  isHandlingApiCall = computed(() => this.isSubmitting() || this.isSavingAsDraft() || this.isInitializing());
  sprintErrorMessage: string | null = null;
  genericErrorMessage: string | null = null;
  isBrowser: boolean;
  newsManangementUrl = '/internal-dashboard/news-management';

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit() {
    if (this.isBrowser) {
      this.pageTitleService.setTitleOnLangChange('common.admin.newsManagement.pageTitle');
    }
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.isCreateMode = false;
        this.selectedId = idParam;
        this.loadReleaseLetterWithDraftCheck(this.selectedId);
      } else {
        this.isCreateMode = true;
      }
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

    this.updateReleaseLetter(this.releaseLetter);
  }

  createReleaseLetter(releaseLetter: ReleaseLetter) {
    this.newsManagementService
      .createReleaseLetter(releaseLetter)
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

  updateReleaseLetter(releaseLetter: ReleaseLetter) {
    this.newsManagementService
      .updateReleaseLetter(releaseLetter.id, releaseLetter)
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

  saveAsDraft() {
    if (this.isSavingAsDraft()) {
      return;
    }

    this.isSavingAsDraft.set(true);
    this.releaseLetter.draftContent = this.releaseLetter.content;
    this.newsManagementService
      .saveAsDraft(this.prepareDraftReleaseLetter())
      .pipe(finalize(() => this.isSavingAsDraft.set(false)))
      .subscribe({
        next: _res => {
          this.router.navigate([this.newsManangementUrl]);
        },
        error: err => {
          this.handleError(err.error.helpCode);
        }
      });
  }

  prepareDraftReleaseLetter(): ReleaseLetter {
    return {
      ...this.releaseLetter,
      content: this.isCreateMode ? '' : this.releaseLetter.content,
      draftContent: this.releaseLetter.content
    };
  }

  handleError(errorHelpCode: string) {
    switch (errorHelpCode) {
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
        this.genericErrorMessage = this.translateService.instant('common.admin.releaseLetterEdit.genericErrorMessage');
        return;
    }
  }

  onClickingBackToNewsManagementButton(): void {
    this.router.navigate([this.newsManangementUrl]);
  }

  onReleaseVersionChange() {
    this.sprintErrorMessage = null;
  }

  loadReleaseLetterWithDraftCheck(id: string) {
    if (this.isHandlingApiCall()) {
      return;
    }

    this.isInitializing.set(true);
    this.newsManagementService
      .getReleaseLetterById(id)
      .pipe(
        switchMap(releaseLetter => {
          this.releaseLetter = releaseLetter;

          return this.newsManagementService.getReleaseLetterDraftExistedByGitHubUserIdAndReleaseLetterId(id);
        }),
        finalize(() => this.isInitializing.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(draft => {
        if (draft !== null) {
          this.appModalService
            .openDraftAlertModal()
            .then(useDraft => {
              if (useDraft) {
                this.releaseLetter.content = draft.draftContent;
              }
            })
            .catch(() => {});
        }
      });
  }
}
