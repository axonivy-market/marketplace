import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  ElementRef,
  Inject,
  inject,
  PLATFORM_ID,
  signal,
  ViewChild,
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
import { Link } from '../../shared/models/apis/link.model';
import { Page } from '../../shared/models/apis/page.model';
import { LoadingService } from '../../core/services/loading/loading.service';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { Subscription, throwError } from 'rxjs';
import { ReleaseLetterCriteria } from '../../shared/models/criteria.model';
import { DEFAULT_PAGEABLE } from '../../shared/constants/common.constant';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-news',
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    NgbAccordionModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './news.component.html',
  styleUrl: './news.component.scss'
})
export class NewsComponent {
  @ViewChild('releaseLetterObserver', { static: false })
  observerElement!: ElementRef;

  isBrowser: boolean;
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  adminDashboardService = inject(AdminDashboardService);
  markdownService = inject(MarkdownService);
  loadingService = inject(LoadingService);
  emptyReleaseLetterTitle = '';
  subscriptions: Subscription[] = [];
  releaseLetterSafeHtmlContentList: WritableSignal<ReleaseLetterSafeHtml[]> =
    signal([]);
  newsLinks!: Link;
  newsPages!: Page;
  releaseLetterCriteria: ReleaseLetterCriteria = {
    pageable: DEFAULT_PAGEABLE
  };
  protected LoadingComponentId = LoadingComponentId;

  constructor(
    @Inject(PLATFORM_ID) private readonly platformId: Object,
    private readonly sanitizer: DomSanitizer
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      this.pageTitleService.setTitleOnLangChange('common.admin.news.pageTitle');
      this.loadReleaseLetters();
    }
  }

  ngAfterViewInit(): void {
    if (this.isBrowser) {
      this.setupIntersectionObserver();
    }
  }

  toSafeHtmlModelList(
    items: ReleaseLetterApiResponse[]
  ): ReleaseLetterSafeHtml[] {
    return items.map(item => this.toSafeHtmlModel(item));
  }

  toSafeHtmlModel(item: ReleaseLetterApiResponse): ReleaseLetterSafeHtml {
    return {
      sprint: item.sprint,
      content: this.renderReleaseLetterContent(item.content),
      latest: item.latest,
      createdAt: item.createdAt
    };
  }

  renderReleaseLetterContent(content: string) {
    const rawHtml = this.markdownService.parseMarkdown(content);
    return this.sanitizer.bypassSecurityTrustHtml(rawHtml);
  }

  getSprintTitle(sprint: string): string {
    return this.translateService.instant('common.admin.news.sprint') + sprint;
  }

  hasMoreReleaseLetters() {
    if (!this.newsLinks || !this.newsPages) {
      return false;
    }
    return (
      this.newsPages.number < this.newsPages.totalPages &&
      this.newsLinks?.next !== undefined
    );
  }

  loadReleaseLetters(): void {
    const sub = this.adminDashboardService
      .getReleaseLetters2(this.releaseLetterCriteria)
      .subscribe({
        next: response => {
          if (!response) {
            return;
          }
          const newReleaseLetters =
            response._embedded?.releaseLetterModelList ?? [];
          if (newReleaseLetters.length == 0) {
            this.emptyReleaseLetterTitle = this.translateService.instant(
              'common.admin.news.emptyLatestReleaseLetter'
            );
          } else {
            this.releaseLetterSafeHtmlContentList.update(
              existingReleaseLetters =>
                existingReleaseLetters.concat(
                  this.toSafeHtmlModelList(newReleaseLetters)
                )
            );
          }
          this.newsLinks = response._links;
          this.newsPages = response.page;
          this.releaseLetterCriteria.nextPageHref = this.newsLinks?.next?.href;
        },
        error: error => throwError(() => error)
      });

    this.subscriptions.push(sub);
  }

  setupIntersectionObserver() {
    if (!this.isBrowser || typeof IntersectionObserver === 'undefined') {
      return;
    }
    const options = { root: null, rootMargin: '10px', threshold: 0.1 };
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (
          entry.isIntersecting &&
          this.hasMoreReleaseLetters() &&
          !this.loadingService.isLoading(LoadingComponentId.NEWS_PAGE)
        ) {
          this.releaseLetterCriteria.nextPageHref = this.newsLinks?.next?.href;
          this.loadReleaseLetters();
        }
      });
    }, options);

    observer.observe(this.observerElement.nativeElement);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => {
      sub.unsubscribe();
    });
  }
}
