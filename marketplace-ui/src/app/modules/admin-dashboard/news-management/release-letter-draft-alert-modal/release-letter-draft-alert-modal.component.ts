import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
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

  constructor(public activeModal: NgbActiveModal) {}

  onConfirm() {
    this.activeModal.close(true);
  }

  onCancel() {
    this.activeModal.close(false);
  }
}
