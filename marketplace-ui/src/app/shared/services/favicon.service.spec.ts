import { TestBed } from '@angular/core/testing';
import { FaviconService } from './favicon.service';
import { DOCUMENT } from '@angular/common';
import { LINK_REL_QUERY } from '../constants/common.constant';

describe('FaviconService', () => {
  let service: FaviconService;
  let document: Document;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FaviconService]
    });
    service = TestBed.inject(FaviconService);
    document = TestBed.inject(DOCUMENT);
  });

  afterEach(() => {
    const links = document.querySelectorAll(LINK_REL_QUERY);
    links.forEach(link => link.remove());
  });

  it('should create a new favicon link if none exists', () => {
    const url = 'https://example.com/icon.png';

    expect(document.querySelector(LINK_REL_QUERY)).toBeNull();

    service.setFavicon(url);

    const link = document.querySelector(LINK_REL_QUERY) as HTMLLinkElement;

    expect(link).not.toBeNull();
    expect(link.href).toBe(url);
  });

  it('should update an existing favicon link', () => {
    const existing = document.createElement('link');
    existing.rel = 'icon';
    existing.href = 'old.ico';
    document.head.appendChild(existing);

    service.setFavicon('new.ico');

    const link = document.querySelector(LINK_REL_QUERY) as HTMLLinkElement;

    expect(link.href).toContain('new.ico');
  });
});
