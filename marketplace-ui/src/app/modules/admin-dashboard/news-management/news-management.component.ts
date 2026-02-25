import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  ElementRef,
  Inject,
  inject,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
  signal,
  ViewChild,
  WritableSignal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { LanguageService } from '../../../core/services/language/language.service';
import { LoadingService } from '../../../core/services/loading/loading.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { DEFAULT_PAGEABLE } from '../../../shared/constants/common.constant';
import { Link } from '../../../shared/models/apis/link.model';
import { Page } from '../../../shared/models/apis/page.model';
import { ReleaseLetterCriteria } from '../../../shared/models/criteria.model';
import { ReleaseLetter } from '../../../shared/models/release-letter-request.model';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { AdminDashboardService } from './../admin-dashboard.service';

@Component({
  selector: 'app-news-management',
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule
  ],
  templateUrl: './news-management.component.html',
  styleUrl: './news-management.component.scss'
})
export class NewsManagementComponent implements OnInit, OnDestroy {
  @ViewChild('releaseLetterObserver', { static: false })
  observerElement!: ElementRef;

  isBrowser: boolean;
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  loadingService = inject(LoadingService);
  adminDashboardService = inject(AdminDashboardService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  subscriptions: Subscription[] = [];
  releaseLetterList: WritableSignal<ReleaseLetter[]> = signal([]);
  appModalService = inject(AppModalService);
  newsLinks!: Link;
  newsPages!: Page;
  releaseLetterCriteria: ReleaseLetterCriteria = {
    pageable: DEFAULT_PAGEABLE
  };
  tableHeadersClass = 'text-primary text-center';

  readonly tableHeaders = [
    { key: '.number', class: 'text-primary' },
    { key: '.sprint', class: this.tableHeadersClass },
    { key: '.createdAt', class: this.tableHeadersClass },
    { key: '.latest', class: this.tableHeadersClass },
    { key: '.actions', class: this.tableHeadersClass }
  ];

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit() {
    if (this.isBrowser) {
      this.pageTitleService.setTitleOnLangChange(
        'common.admin.newsManagement.pageTitle'
      );
      this.loadReleaseLetters();
    }
  }

  navigateToEditPage(releaseVersion: string) {
    this.router.navigate(['edit', releaseVersion], {
      relativeTo: this.route
    });
  }

  openModal(item: ReleaseLetter) {
    const buttonElement = document.activeElement as HTMLElement;
    buttonElement.blur();
    this.appModalService.openReleaseLetterModal(item);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString();
  }

  openDeleteConfirmModal(sprint: string) {
    const buttonElement = document.activeElement as HTMLElement;
    buttonElement.blur();
    this.appModalService
      .openDeleteReleaseLetterConfirmModal(sprint)
      .then(() => {
        this.adminDashboardService
          .getReleaseLettersWithoutPaging()
          .subscribe(res => {
            this.releaseLetterList.set(res._embedded.releaseLetterModelList);
          });
      });
  }

  loadReleaseLetters(): void {
    const sub = this.adminDashboardService
      .getReleaseLettersWithoutPaging()
      .subscribe({
        next: response => {
          if (!response) {
            return;
          }
          const newReleaseLetters =
            response._embedded?.releaseLetterModelList ?? [];
          if (newReleaseLetters.length > 0) {
            this.releaseLetterList.update(existingReleaseLetters =>
              existingReleaseLetters.concat(newReleaseLetters)
            );
          }
        }
      });

    this.subscriptions.push(sub);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => {
      sub.unsubscribe();
    });
  }
}
