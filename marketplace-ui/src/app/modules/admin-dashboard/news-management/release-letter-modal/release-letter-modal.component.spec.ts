import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReleaseLetterModalComponent } from './release-letter-modal.component';
import { MarkdownService } from '../../../../shared/services/markdown.service';
import { TranslateService } from '@ngx-translate/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AdminDashboardService } from '../../admin-dashboard.service';
import { of } from 'rxjs';

const mockResponse = {
  id: '123',
  sprint: 'S43',
  content: 'content',
  latest: true,
  createdAt: '2026-02-01',
  updatedAt: '2026-02-02'
};

describe('ReleaseLetterModalComponent', () => {
  let component: ReleaseLetterModalComponent;
  let fixture: ComponentFixture<ReleaseLetterModalComponent>;

  let markdownServiceMock: jasmine.SpyObj<MarkdownService>;
  let translateServiceMock: jasmine.SpyObj<TranslateService>;
  let adminDashboardServiceMock: jasmine.SpyObj<AdminDashboardService>;
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
    adminDashboardServiceMock = jasmine.createSpyObj('AdminDashboardService', [
      'getReleaseLetterById'
    ]);

    adminDashboardServiceMock.getReleaseLetterById.and.returnValue(
      of(mockResponse)
    );
    markdownServiceMock.parseMarkdown.and.returnValue('<p>Mock</p>');
    translateServiceMock.instant.and.returnValue('Sprint: ');

    await TestBed.configureTestingModule({
      imports: [ReleaseLetterModalComponent],
      providers: [
        { provide: MarkdownService, useValue: markdownServiceMock },
        { provide: TranslateService, useValue: translateServiceMock },
        { provide: NgbActiveModal, useValue: activeModalMock },
        { provide: AdminDashboardService, useValue: adminDashboardServiceMock }
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
    expect(adminDashboardServiceMock.getReleaseLetterById).toHaveBeenCalledWith(
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
