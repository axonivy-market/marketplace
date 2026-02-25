import { Injectable } from '@angular/core';
import MarkdownIt from 'markdown-it';
import taskLists from 'markdown-it-task-lists';
import { full } from 'markdown-it-emoji';
import * as MarkdownItGitHubAlerts from 'markdown-it-github-alerts';
import LinkifyIt from 'linkify-it';

const GITHUB_BASE_URL = 'https://github.com/';

type MarkdownItPlugin = (md: MarkdownIt, ...params: unknown[]) => void;

@Injectable({
  providedIn: 'root'
})
export class MarkdownService {
  private readonly md: MarkdownIt;

  constructor() {
    const githubAlertsPlugin =
      (MarkdownItGitHubAlerts as unknown as { default?: MarkdownItPlugin })
        .default ?? (MarkdownItGitHubAlerts as unknown as MarkdownItPlugin);

    this.md = new MarkdownIt({
      html: true
    });
    this.md
      .use(full)
      // .use(
      //   (MarkdownItGitHubAlerts as any).default ??
      //     (MarkdownItGitHubAlerts as any)
      // )
      .use(githubAlertsPlugin)
      .use(taskLists)
      .use(this.linkifyPullRequests)
      .set({
        typographer: true,
        linkify: true
      })
      .enable(['smartquotes', 'replacements', 'image']);
  }

  parseMarkdown(markdownText: string): string {
    return this.md.render(markdownText);
  }

  linkifyPullRequests(
    md: MarkdownIt,
    sourceUrl: string,
    prNumberRegex: RegExp
  ) {
    md.renderer.rules.text = (tokens, idx) => {
      const content = tokens[idx].content;
      const linkify = new LinkifyIt();
      const matches = linkify.match(content);

      if (!matches) {
        return content;
      }

      let result = content;

      matches.forEach(match => {
        const url = match.url;

        if (url.startsWith(`${sourceUrl}/compare/`)) {
          return;
        }
        if (url.startsWith(sourceUrl)) {
          const pullNumberMatch = prNumberRegex.exec(url);
          let pullNumber = null;

          if (pullNumberMatch) {
            pullNumber = pullNumberMatch[1];
            const start = match.index;
            const end = start + match.lastIndex - match.index;
            const link = `#${pullNumber}`;

            result = result.slice(0, start) + link + result.slice(end);
          }
        } else if (url.startsWith(GITHUB_BASE_URL)) {
          const username = url.replace(GITHUB_BASE_URL, '');

          const mention = `@${username}`;
          result = result.replace(url, mention);
        } else {
          return;
        }
      });

      return result;
    };
  }
}
