import { Component, inject, Input, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import MarkdownIt from 'markdown-it';
import { MarkdownService } from '../../../../shared/services/markdown.service';
import { AdminDashboardService } from '../../admin-dashboard.service';

@Component({
  selector: 'app-release-letter-modal',
  imports: [TranslateModule],
  templateUrl: './release-letter-modal.component.html',
  styleUrl: './release-letter-modal.component.scss'
})
export class ReleaseLetterModalComponent implements OnInit {
  @Input()
  sprint!: string;

  markdownService = inject(MarkdownService);
  translateService = inject(TranslateService);
  adminDashboardService = inject(AdminDashboardService);

  md: MarkdownIt = new MarkdownIt();
  sprintHeader = '';
  releaseLetterContent: SafeHtml = '';

  constructor(
    public activeModal: NgbActiveModal,
    private readonly sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    console.log(this.sprint);
    this.sprintHeader = this.getSprintHeader();
    this.adminDashboardService.getReleaseLetterBySprint(this.sprint).subscribe(response => {
      this.releaseLetterContent = this.renderReleaseLetterContent(response.content!);
    });
  }

  renderReleaseLetterContent(content: string) {
    const rawHtml = this.markdownService.parseMarkdown(content);
    return this.sanitizer.bypassSecurityTrustHtml(rawHtml);
  }

  getSprintHeader() {
    return (
      this.translateService.instant(
        'common.admin.newsManagement.sprintHeader'
      ) + this.sprint
    );
  }
}
