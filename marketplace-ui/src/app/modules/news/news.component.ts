import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, inject, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../core/services/language/language.service';
import { ThemeService } from '../../core/services/theme/theme.service';
import { PageTitleService } from '../../shared/services/page-title.service';
import { AdminDashboardService } from '../admin-dashboard/admin-dashboard.service';
import { MarkdownService } from '../../shared/services/markdown.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-news',
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  templateUrl: './news.component.html',
  styleUrl: './news.component.scss'
})
export class NewsComponent {
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  adminDashboardService = inject(AdminDashboardService);
  markdownService = inject(MarkdownService);
  isBrowser: boolean;
  activeReleaseLetterSprintTitle = '';
  activeReleaseLetterContent: SafeHtml = '';
  constructor(
    @Inject(PLATFORM_ID) private readonly platformId: Object,
    private readonly sanitizer: DomSanitizer
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      this.pageTitleService.setTitleOnLangChange('common.admin.news.pageTitle');
    }
    this.adminDashboardService.getActiveRelaseLetters().subscribe(res => {
      if (res._embedded.releaseLetterModelList.length == 0) {
        this.activeReleaseLetterSprintTitle = this.translateService.instant(
          'common.admin.news.emptyActiveReleaseLetter'
        );
      } else {
        const response = res._embedded.releaseLetterModelList[0];
        this.activeReleaseLetterSprintTitle = this.getSprintTitle(
          response.sprint
        );
        this.activeReleaseLetterContent =
          this.sanitizer.bypassSecurityTrustHtml(
            this.markdownService.parseMarkdown(response.content)
          );
      }
    });
  }

  getSprintTitle(sprint: string): string {
    return this.translateService.instant('common.admin.news.sprint') + sprint;
  }
}
