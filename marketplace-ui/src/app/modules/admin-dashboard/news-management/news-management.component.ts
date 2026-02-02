import {
  afterNextRender,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Inject,
  inject,
  NgZone,
  PLATFORM_ID,
  ViewChild
} from '@angular/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { MarkdownEditorComponent } from '../../../shared/components/markdown-editor/markdown-editor.component';

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
  easyMDE!: EasyMDE;
  body: string = 'abc';

  onSubmit() {
    console.log('Abc');
  }
}
