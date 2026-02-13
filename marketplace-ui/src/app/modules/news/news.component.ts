import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  Inject,
  inject,
  PLATFORM_ID,
  signal,
  WritableSignal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { NgbAccordionModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../core/services/language/language.service';
import { ThemeService } from '../../core/services/theme/theme.service';
import { ReleaseLetterApiResponse } from '../../shared/models/apis/release-letter-response.model';
import { ReleaseLetterSafeHtml } from '../../shared/models/release-letter-safe-html-model';
import { MarkdownService } from '../../shared/services/markdown.service';
import { PageTitleService } from '../../shared/services/page-title.service';
import { AdminDashboardService } from '../admin-dashboard/admin-dashboard.service';

@Component({
  selector: 'app-news',
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    NgbAccordionModule
  ],
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
  emptyReleaseLetterTitle= '';
  releaseLetterSafeHtmlContentList: WritableSignal<ReleaseLetterSafeHtml[]> =
    signal([]);

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

    this.adminDashboardService.getRelaseLetters().subscribe({
      next: res => {
        if (res._embedded.releaseLetterModelList.length == 0) {
          this.emptyReleaseLetterTitle = this.translateService.instant(
            'common.admin.news.emptyLatestReleaseLetter'
          );
        }
        res._embedded.releaseLetterModelList.forEach(item => {
          const releaseLetterSafeHtl: ReleaseLetterSafeHtml = {
            sprint: item.sprint,
            content: this.sanitizer.bypassSecurityTrustHtml(
              this.markdownService.parseMarkdown(item.content)
            ),
            latest: item.latest,
            createdAt: item.createdAt
          };
          this.releaseLetterSafeHtmlContentList.update(existedList =>
            existedList.concat(releaseLetterSafeHtl)
          );
        });
      }
    });
  }

  renderReleaseLetterContent(content: string) {
    const rawHtml = this.markdownService.parseMarkdown(content);
    return this.sanitizer.bypassSecurityTrustHtml(rawHtml);
  }

  toSafeHtmlModel(item: ReleaseLetterApiResponse): ReleaseLetterSafeHtml {
    console.log(item);

    return {
      sprint: item.sprint,
      content: this.renderReleaseLetterContent(item.content),
      latest: item.latest,
      createdAt: item.createdAt
    };
  }

  getSprintTitle(sprint: string): string {
    return this.translateService.instant('common.admin.news.sprint') + sprint;
  }
}
