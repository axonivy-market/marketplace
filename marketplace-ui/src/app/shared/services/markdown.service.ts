import { Injectable } from '@angular/core';
import MarkdownIt from 'markdown-it';
import { full } from 'markdown-it-emoji';
import MarkdownItGitHubAlerts from 'markdown-it-github-alerts';
import LinkifyIt from 'linkify-it';

@Injectable({
  providedIn: 'root'
})
export class MarkdownService {

  private md: MarkdownIt;

  constructor() {
    this.md = new MarkdownIt();
    this.md.use(full);
    this.md.use(MarkdownItGitHubAlerts);
    this.md.renderer.rules.text = (tokens, idx) => {
      return tokens[idx].content;
    };
  }

  parseMarkdown(markdownText: string): string {
    return this.md.render(markdownText);
  }
}
