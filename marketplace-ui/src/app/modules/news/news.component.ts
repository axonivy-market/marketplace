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
import { ReleaseLetter } from '../../shared/models/release-letter-request.model';

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
  activeReleaseLetter: ReleaseLetter = {
    sprint: '',
    content: '',
    active: false
  };

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      this.pageTitleService.setTitleOnLangChange('common.admin.news.pageTitle');
    }
    this.adminDashboardService.getActiveRelaseLetters().subscribe(res => {
      if (res._embedded.releaseLetterModelList.length == 0) {
        this.activeReleaseLetter.active = this.translateService.instant(
          'common.admin.news.emptyActiveReleaseLetter'
        );
      } else {
        const response = res._embedded.releaseLetterModelList[0];
        this.activeReleaseLetterSprintTitle = this.getSprintTitle(response.sprint);
        this.activeReleaseLetter.content = this.markdownService.parseMarkdown(
          response.content
        );
        this.activeReleaseLetter.sprint = response.sprint;
        this.activeReleaseLetter.active = response.active;
      }
    });
  }

  getSprintTitle(sprint: string): string {
    return this.translateService.instant('common.admin.news.sprint') + sprint;
  }
}
