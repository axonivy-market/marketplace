import { Component, inject, Input } from '@angular/core';
import { ReleaseLetter } from '../../../../shared/models/release-letter-request.model';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import MarkdownIt from 'markdown-it';
import { DomSanitizer } from '@angular/platform-browser';
import { MarkdownService } from '../../../../shared/services/markdown.service';

@Component({
  selector: 'app-release-letter-modal',
  imports: [],
  templateUrl: './release-letter-modal.component.html',
  styleUrl: './release-letter-modal.component.scss'
})
export class ReleaseLetterModalComponent {
  @Input() 
  item!: ReleaseLetter;

  markdownService = inject(MarkdownService);

  md: MarkdownIt = new MarkdownIt();

  constructor(
    public activeModal: NgbActiveModal,
    private readonly sanitizer: DomSanitizer
  ) {}

  renderReleaseLetterContent() {
    const rawHtml = this.markdownService.parseMarkdown(this.item.content || '');
    return this.sanitizer.bypassSecurityTrustHtml(rawHtml);
  } 
}
