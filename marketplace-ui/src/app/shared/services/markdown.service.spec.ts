import { TestBed } from '@angular/core/testing';

import { MarkdownService } from './markdown.service';

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
});
