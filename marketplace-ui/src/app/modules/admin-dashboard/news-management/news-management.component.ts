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
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { ReleaseLetter } from '../../../shared/models/release-letter-request.model';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { NEWS_MANAGEMENT_MODE } from './../../../shared/constants/query.params.constant';
import { AdminDashboardService } from './../admin-dashboard.service';
import { AppModalService } from '../../../shared/services/app-modal.service';

@Component({
  selector: 'app-news-management',
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
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
  route = inject(ActivatedRoute);
  easyMDE!: EasyMDE;
  releaseLetterValue = 'abc';
  releaseVersion = '';
  MODE = NEWS_MANAGEMENT_MODE;
  currentMode: WritableSignal<string> = signal(NEWS_MANAGEMENT_MODE.view);
  currentModePlain = NEWS_MANAGEMENT_MODE.view;
  releaseLetterList: WritableSignal<ReleaseLetter[]> = signal([]);
  appModalService = inject(AppModalService);
  isBrowser: boolean;

  readonly tableHeaders = [
    { key: '.number', class: 'text-primary' },
    { key: '.sprint', class: 'text-primary text-center' },
    { key: '.active', class: 'text-primary text-center' },
    { key: '.actions', class: 'text-primary text-center' }
  ];

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit() {
    if (this.isBrowser) {
      this.pageTitleService.setTitleOnLangChange(
        'common.admin.newsManagement.pageTitle'
      );
    }
    this.adminDashboardService.getRelaseLetters().subscribe(res => {
      this.releaseLetterList.set(res._embedded.releaseLetterModelList);
    });
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
}
