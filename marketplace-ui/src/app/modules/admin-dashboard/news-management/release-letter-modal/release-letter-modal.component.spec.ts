import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { NewsManagementService } from './../news-management.service';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { MarkdownService } from '../../../../shared/services/markdown.service';
import { ReleaseLetterModalComponent } from './release-letter-modal.component';

const mockResponse = {
  id: '123',
  sprint: 'S43',
  content: 'content',
  hasDraft: false,
  latest: true,
  createdAt: '2026-02-01',
  updatedAt: '2026-02-02'
};

describe('ReleaseLetterModalComponent', () => {
  let component: ReleaseLetterModalComponent;
  let fixture: ComponentFixture<ReleaseLetterModalComponent>;

  let markdownServiceMock: MockedObject<MarkdownService>;
  let translateServiceMock: MockedObject<TranslateService>;
  let newsManagementServiceMock: MockedObject<NewsManagementService>;
  let activeModalMock: MockedObject<NgbActiveModal>;

  beforeEach(async () => {
    markdownServiceMock = {
      parseMarkdown: vi.fn().mockName('MarkdownService.parseMarkdown')
    } as any;
    translateServiceMock = {
      instant: vi.fn().mockName('TranslateService.instant')
    } as any;
    activeModalMock = {
      close: vi.fn().mockName('NgbActiveModal.close'),
      dismiss: vi.fn().mockName('NgbActiveModal.dismiss')
    } as any;
    newsManagementServiceMock = {
      getReleaseLetterById: vi
        .fn()
        .mockName('NewsManagementService.getReleaseLetterById')
    } as any;

    newsManagementServiceMock.getReleaseLetterById.mockReturnValue(
      of(mockResponse)
    );
    markdownServiceMock.parseMarkdown.mockReturnValue('<p>Mock</p>');
    translateServiceMock.instant.mockReturnValue('Sprint: ');

    await TestBed.configureTestingModule({
      imports: [ReleaseLetterModalComponent],
      providers: [
        { provide: MarkdownService, useValue: markdownServiceMock },
        { provide: TranslateService, useValue: translateServiceMock },
        { provide: NgbActiveModal, useValue: activeModalMock },
        { provide: NewsManagementService, useValue: newsManagementServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReleaseLetterModalComponent);
    component = fixture.componentInstance;

    component.id = '123';

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getReleaseLetterById on init', () => {
    expect(newsManagementServiceMock.getReleaseLetterById).toHaveBeenCalledWith(
      '123'
    );
  });

  it('should set sprintHeader correctly', () => {
    expect(translateServiceMock.instant).toHaveBeenCalledWith(
      'common.admin.newsManagement.sprintHeader'
    );

    expect(component.sprintHeader).toBe('Sprint: ' + mockResponse.sprint);
  });

  it('should parse markdown and sanitize content', () => {
    expect(markdownServiceMock.parseMarkdown).toHaveBeenCalledWith(
      mockResponse.content
    );
    expect(component.releaseLetterContent).toBeTruthy();
  });

  it('getSprintHeader should concatenate translated label and sprint', () => {
    const header = component.getSprintHeader(mockResponse.sprint);

    expect(translateServiceMock.instant).toHaveBeenCalledWith(
      'common.admin.newsManagement.sprintHeader'
    );

    expect(header).toBe('Sprint: ' + mockResponse.sprint);
  });
});
