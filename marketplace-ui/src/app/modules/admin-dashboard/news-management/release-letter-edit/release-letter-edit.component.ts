import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MarkdownEditorComponent } from '../../../../shared/components/markdown-editor/markdown-editor.component';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
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
export class ReleaseLetterEditComponent {
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  adminDashboardService = inject(AdminDashboardService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  easyMDE!: EasyMDE;
  releaseLetterValue = '';
  releaseVersion = '';

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.releaseVersion = params.get('release-version') ?? '';
      this.getReleaseLetter();
    });
  }

  getReleaseLetter(): void {
    this.adminDashboardService
      .getRelaseLetterByReleaseVersion(this.releaseVersion)
      .subscribe(response => {
        console.log(response);

        this.releaseLetterValue = response.content;
      });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.router.navigate(['/internal-dashboard/news-management']);
  }

  onClickingBackToNewsManagementButton(): void {
    this.router.navigate(['/internal-dashboard/news-management']);
  }
}
