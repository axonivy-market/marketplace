import { AdminDashboardService } from './../admin-dashboard.service';
import { NEWS_MANAGEMENT_MODE } from './../../../shared/constants/query.params.constant';
import { CommonModule } from '@angular/common';
import { Component, ElementRef, inject, signal, Signal, ViewChild, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { MarkdownEditorComponent } from '../../../shared/components/markdown-editor/markdown-editor.component';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { ReleaseLetterResponse } from '../../../shared/models/apis/release-letter-response.model';

@Component({
  selector: 'app-news-management',
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    MarkdownEditorComponent
  ],
  templateUrl: './news-management.component.html',
  styleUrl: './news-management.component.scss'
})
export class NewsManagementComponent {
  @ViewChild('editor') editor!: ElementRef<HTMLTextAreaElement>;

  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  adminDashboardService = inject(AdminDashboardService);
  router = inject(Router);
  easyMDE!: EasyMDE;
  releaseLetterValue = 'abc';
  releaseVersion = '';
  MODE = NEWS_MANAGEMENT_MODE;
  currentMode: WritableSignal<string> = signal(NEWS_MANAGEMENT_MODE.view);
  currentModePlain = NEWS_MANAGEMENT_MODE.view;
  releaseLetterList: WritableSignal<ReleaseLetterResponse[]> = signal([]);

  ngOnInit() {
    this.adminDashboardService.getRelaseLetters().subscribe(res => {
      console.log(res);
      this.releaseLetterList.set(res._embedded.releaseLetterModelList);
    })
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.router.navigate(['/internal-dashboard']);
  }

  navigateToEditMode(releaseVersion: string) {
    console.log(releaseVersion);
    this.router.navigate([], {
      // queryParams: {
      //   [NEWS_MANAGEMENT_MODE.mode]: NEWS_MANAGEMENT_MODE.edit,
      //   [NEWS_MANAGEMENT_MODE.releaseVersion]: releaseVersion
      // },
      // queryParamsHandling: 'merge'
    });
  }
}
