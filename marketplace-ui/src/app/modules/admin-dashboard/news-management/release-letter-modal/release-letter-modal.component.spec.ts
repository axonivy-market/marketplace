import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReleaseLetterModalComponent } from './release-letter-modal.component';
import { MarkdownService } from '../../../../shared/services/markdown.service';
import { TranslateService } from '@ngx-translate/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SafeHtml } from '@angular/platform-browser';

describe('ReleaseLetterModalComponent', () => {
  let component: ReleaseLetterModalComponent;
  let fixture: ComponentFixture<ReleaseLetterModalComponent>;

  let markdownServiceMock: jasmine.SpyObj<MarkdownService>;
  let translateServiceMock: jasmine.SpyObj<TranslateService>;
  let activeModalMock: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    markdownServiceMock = jasmine.createSpyObj('MarkdownService', [
      'parseMarkdown'
    ]);
    translateServiceMock = jasmine.createSpyObj('TranslateService', [
      'instant'
    ]);
    activeModalMock = jasmine.createSpyObj('NgbActiveModal', [
      'close',
      'dismiss'
    ]);
    markdownServiceMock.parseMarkdown.and.returnValue('<p>Mock</p>');
    translateServiceMock.instant.and.returnValue('Sprint: ');

    await TestBed.configureTestingModule({
      imports: [ReleaseLetterModalComponent],
      providers: [
        { provide: MarkdownService, useValue: markdownServiceMock },
        { provide: TranslateService, useValue: translateServiceMock },
        { provide: NgbActiveModal, useValue: activeModalMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReleaseLetterModalComponent);
    component = fixture.componentInstance;

    component.item = {
      sprint: 'S43',
      content: '# Hello'
    } as any;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize sprintHeader correctly', () => {
    expect(translateServiceMock.instant).toHaveBeenCalledWith(
      'common.admin.newsManagement.sprintHeader'
    );

    expect(component.sprintHeader).toBe('Sprint: S43');
  });

  it('should render markdown and sanitize content on init', () => {
    expect(markdownServiceMock.parseMarkdown).toHaveBeenCalledWith('# Hello');
    expect(component.releaseLetterContent).toBeTruthy();
  });

  it('renderReleaseLetterContent should return SafeHtml', () => {
    markdownServiceMock.parseMarkdown.calls.reset();
    markdownServiceMock.parseMarkdown.and.returnValue('<p>New</p>');

    const result: SafeHtml = component.renderReleaseLetterContent();

    expect(markdownServiceMock.parseMarkdown).toHaveBeenCalledWith('# Hello');
    expect(result).toBeTruthy();
  });

  it('getSprintHeader should concatenate translation and sprint', () => {
    translateServiceMock.instant.and.returnValue('Version: ');

    const result = component.getSprintHeader();

    expect(result).toBe('Version: S43');
  });
});
