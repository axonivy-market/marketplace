import { TestBed } from '@angular/core/testing';

import { MarkdownService } from './markdown.service';
import MarkdownIt from 'markdown-it';
import { GITHUB_PULL_REQUEST_NUMBER_REGEX } from '../constants/common.constant';

describe('MarkdownService', () => {
  let service: MarkdownService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MarkdownService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should render basic Markdown correctly', () => {
    const input = '# Hello World';
    const expectedOutput = '<h1>Hello World</h1>\n';
    expect(service.parseMarkdown(input)).toBe(expectedOutput);
  });

  it('should convert bold and italic Markdown syntax', () => {
    const input = '**bold** _italic_';
    const expectedOutput = '<p><strong>bold</strong> <em>italic</em></p>\n';
    expect(service.parseMarkdown(input)).toBe(expectedOutput);
  });

  it('should render emoji using markdown-it-emoji', () => {
    const input = 'Hello :smile:';
    const expectedOutput = '<p>Hello 😄</p>\n'; // Emoji gets converted
    expect(service.parseMarkdown(input)).toBe(expectedOutput);
  });

  it('should allow safe anchor tags', () => {
    const input = '[Google](https://google.com)';
    const result = service.parseMarkdown(input);
    expect(result).toContain('<a href="https://google.com">Google</a>');
  });

  it('should not allow javascript: links (prevent phishing attacks)', () => {
    const input = '[Click me](javascript:alert("Hacked!"))';
    const result = service.parseMarkdown(input);
    expect(result).not.toContain('href="javascript:alert');
  });

  it('should replace GitHub URLs with appropriate links in linkifyPullRequests', () => {
    const md = new MarkdownIt();
    const sourceUrl = 'https://github.com/source-repo';
    service.linkifyPullRequests(
      md,
      sourceUrl,
      GITHUB_PULL_REQUEST_NUMBER_REGEX
    );

    const inputText =
      'Check out this PR: https://github.com/source-repo/pull/123';
    const expectedOutput = 'Check out this PR: #123';
    const result = md.renderInline(inputText);

    expect(result).toContain(expectedOutput);
  });

  it('should keep GitHub URLs if they contain compare string in linkifyPullRequests', () => {
    const md = new MarkdownIt();
    const sourceUrl = 'https://github.com/source-repo';
    service.linkifyPullRequests(
      md,
      sourceUrl,
      GITHUB_PULL_REQUEST_NUMBER_REGEX
    );

    const inputText =
      'Check out this PR: https://github.com/source-repo/compare/123';
    const expectedOutput =
      'Check out this PR: https://github.com/source-repo/compare/123';
    const result = md.renderInline(inputText);

    expect(result).toContain(expectedOutput);
  });

  it('should convert GitHub profile URL to @mention in linkifyPullRequests', () => {
    const md = new MarkdownIt();
    const sourceUrl = 'https://github.com/source-repo';

    service.linkifyPullRequests(
      md,
      sourceUrl,
      GITHUB_PULL_REQUEST_NUMBER_REGEX
    );

    const inputText =
      'Thanks to https://github.com/johndoe for the contribution';
    const result = md.renderInline(inputText);

    expect(result).toContain('Thanks to @johndoe for the contribution');
  });

  it('should leave non-GitHub URLs unchanged in linkifyPullRequests', () => {
    const md = new MarkdownIt();
    const sourceUrl = 'https://github.com/source-repo';

    service.linkifyPullRequests(
      md,
      sourceUrl,
      GITHUB_PULL_REQUEST_NUMBER_REGEX
    );

    const inputText = 'Visit https://example.com for more info';

    const result = md.renderInline(inputText);

    expect(result).toContain('https://example.com');
  });
});
