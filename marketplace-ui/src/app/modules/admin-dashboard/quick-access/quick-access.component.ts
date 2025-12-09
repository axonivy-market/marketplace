import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { PageTitleService } from '../../../shared/services/page-title.service';

@Component({
  selector: 'app-quick-access',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    DragDropModule
  ],
  templateUrl: './quick-access.component.html',
  styleUrls: ['./quick-access.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class QuickAccessComponent implements OnInit {
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);

  ngOnInit(): void {
    this.pageTitleService.setTitleOnLangChange(
      'common.admin.quickAccess.pageTitle'
    );
  }
}
