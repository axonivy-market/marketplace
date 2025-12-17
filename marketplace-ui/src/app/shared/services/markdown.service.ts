import { Injectable } from '@angular/core';
import MarkdownIt from 'markdown-it';
import { full } from 'markdown-it-emoji';
import * as MarkdownItGitHubAlerts from 'markdown-it-github-alerts';

@Injectable({
  providedIn: 'root'
})
export class MarkdownService {

  private readonly md: MarkdownIt;

  constructor() {
    this.md = new MarkdownIt({
      html: true
    });
    this.md.use(full);
    const githubAlertsPlugin = 
      MarkdownItGitHubAlerts.default ?? MarkdownItGitHubAlerts;
    this.md.use(githubAlertsPlugin);
  }

  parseMarkdown(markdownText: string): string {
    return this.md.render(markdownText);
  }
}