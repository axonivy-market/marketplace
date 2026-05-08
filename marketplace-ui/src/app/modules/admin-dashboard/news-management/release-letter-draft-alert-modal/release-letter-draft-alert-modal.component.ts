import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';

@Component({
  selector: 'app-release-letter-draft-alert-modal',
  imports: [CommonModule, TranslateModule],
  templateUrl: './release-letter-draft-alert-modal.component.html',
  styleUrl: './release-letter-draft-alert-modal.component.scss'
})
export class ReleaseLetterDraftAlertModalComponent {
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);

  constructor(
    public activeModal: NgbActiveModal,
    private readonly sanitizer: DomSanitizer
  ) {}

  onConfirm() {
    this.activeModal.close(true);
  }

  // NO button
  onCancel() {
    this.activeModal.close(false);
  }
}
