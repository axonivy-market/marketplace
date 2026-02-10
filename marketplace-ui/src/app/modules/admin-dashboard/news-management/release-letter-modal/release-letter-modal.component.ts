import { Component, inject, Input } from '@angular/core';
import { ReleaseLetter } from '../../../../shared/models/release-letter-request.model';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import MarkdownIt from 'markdown-it';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { MarkdownService } from '../../../../shared/services/markdown.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-release-letter-modal',
  imports: [TranslateModule],
  templateUrl: './release-letter-modal.component.html',
  styleUrl: './release-letter-modal.component.scss'
})
export class ReleaseLetterModalComponent {
  @Input()
  item!: ReleaseLetter;

  markdownService = inject(MarkdownService);
  translateService = inject(TranslateService);

  md: MarkdownIt = new MarkdownIt();
  sprintHeader = '';
  releaseLetterContent: SafeHtml = '';

  constructor(
    public activeModal: NgbActiveModal,
    private readonly sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    this.sprintHeader = this.getSprintHeader();
    this.releaseLetterContent = this.renderReleaseLetterContent();
  }

  renderReleaseLetterContent() {
    const rawHtml = this.markdownService.parseMarkdown(this.item.content);
    return this.sanitizer.bypassSecurityTrustHtml(rawHtml);
  }

  getSprintHeader() {
    return this.translateService.instant('common.admin.newsManagement.sprintHeader') + this.item.sprint;
  }
}
