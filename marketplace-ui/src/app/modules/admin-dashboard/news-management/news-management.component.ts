import { AdminDashboardService } from './../admin-dashboard.service';
import { NEWS_MANAGEMENT_MODE } from './../../../shared/constants/query.params.constant';
import { CommonModule } from '@angular/common';
import {
  Component,
  ElementRef,
  inject,
  signal,
  Signal,
  ViewChild,
  WritableSignal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { MarkdownEditorComponent } from '../../../shared/components/markdown-editor/markdown-editor.component';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { ReleaseLetterApiResponse } from '../../../shared/models/apis/release-letter-response.model';
import { ReleaseLetter } from '../../../shared/models/release-letter-request.model';

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
  route = inject(ActivatedRoute);
  easyMDE!: EasyMDE;
  releaseLetterValue = 'abc';
  releaseVersion = '';
  MODE = NEWS_MANAGEMENT_MODE;
  currentMode: WritableSignal<string> = signal(NEWS_MANAGEMENT_MODE.view);
  currentModePlain = NEWS_MANAGEMENT_MODE.view;
  releaseLetterList: WritableSignal<ReleaseLetter[]> = signal([]);

  readonly tableHeaders = [
    { key: '.number', class: 'text-primary' },
    { key: '.releaseVersion', class: 'text-primary' },
    { key: '.actions', class: 'text-primary text-center' }
  ];

  ngOnInit() {
    this.adminDashboardService.getRelaseLetters().subscribe(res => {
      this.releaseLetterList.set(res._embedded.releaseLetterModelList);
    });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.router.navigate(['/internal-dashboard']);
  }

  navigateToEditPage(releaseVersion: string) {
    this.router.navigate(['edit', releaseVersion], {
      relativeTo: this.route
    });
  }
}
