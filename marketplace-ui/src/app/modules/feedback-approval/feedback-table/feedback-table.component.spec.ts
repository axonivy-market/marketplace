import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeedbackTableComponent } from './feedback-table.component';
import { LanguageService } from '../../../core/services/language/language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Feedback } from '../../../shared/models/feedback.model';
import { By } from '@angular/platform-browser';
import { FeedbackStatus } from '../../../shared/enums/feedback-status.enum';

// Mock LanguageService
class MockLanguageService {
  selectedLanguage() {
    return 'en';
  }
}

describe('FeedbackTableComponent', () => {
  let component: FeedbackTableComponent;
  let fixture: ComponentFixture<FeedbackTableComponent>;
  let languageService: LanguageService;

  // Sample feedback data
  const mockFeedbacks: Feedback[] = [
    {
      id: '1',
      username: 'testUser',
      userAvatarUrl: 'http://test.com/avatar.jpg',
      content: 'Great product!',
      rating: 5,
      feedbackStatus: FeedbackStatus.PENDING,
      createdAt: new Date('2025-01-01'),
      updatedAt: new Date('2025-01-02'),
      moderatorName: 'mod1',
      reviewDate: new Date('2025-01-03'),
      productId: '123'
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        TranslateModule.forRoot(),
        FeedbackTableComponent
      ],
      providers: [
        { provide: LanguageService, useClass: MockLanguageService },
        TranslateService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FeedbackTableComponent);
    component = fixture.componentInstance;
    component.feedbacks = mockFeedbacks;
    fixture.detectChanges();
  });

  it('should emit reviewAction with correct feedback and approval status when called', () => {
    const feedback = mockFeedbacks[0];
    const approveSpy = spyOn(component.reviewAction, 'emit');

    component.handleReviewAction(feedback, true);

    expect(approveSpy).toHaveBeenCalledWith({
      feedback: feedback,
      approved: true
    });
  });

  it('should emit reviewAction with false approval status when rejected', () => {
    const feedback = mockFeedbacks[0];
    const rejectSpy = spyOn(component.reviewAction, 'emit');

    component.handleReviewAction(feedback, false);

    expect(rejectSpy).toHaveBeenCalledWith({
      feedback: feedback,
      approved: false
    });
  });


  it('should display all table headers when isHistory is false', () => {
    component.isHistoryTab = false;
    fixture.detectChanges();

    const headers = fixture.debugElement.queryAll(By.css('th'));
    expect(headers.length).toBe(7); // 6 standard + 1 action column
    expect(headers[6].nativeElement.textContent).toContain('common.approval.action');
  });

  it('should display history-specific headers when isHistory is true', () => {
    component.isHistoryTab = true;
    fixture.detectChanges();

    const headers = fixture.debugElement.queryAll(By.css('th'));
    expect(headers.length).toBe(8); // 6 standard + 2 history columns
    expect(headers[6].nativeElement.textContent).toContain('common.approval.moderator');
    expect(headers[7].nativeElement.textContent).toContain('common.approval.reviewDate');
  });

  it('should render feedback data correctly', () => {
    const rows = fixture.debugElement.queryAll(By.css('tbody tr'));
    expect(rows.length).toBe(1);

    const cells = rows[0].queryAll(By.css('td'));
    expect(cells[0].query(By.css('.feedback-username')).nativeElement.textContent).toContain('testUser');
    expect(cells[1].nativeElement.textContent).toContain('Great product!');
    expect(cells[2].nativeElement.textContent).toContain('5');
    expect(cells[3].nativeElement.textContent).toContain(FeedbackStatus.PENDING);
  });

  it('should show action buttons when isHistory is false', () => {
    component.isHistoryTab = false;
    fixture.detectChanges();

    const actionCell = fixture.debugElement.query(By.css('.action-buttons'));
    expect(actionCell).toBeTruthy();
    expect(actionCell.query(By.css('#approve-button'))).toBeTruthy();
    expect(actionCell.query(By.css('#reject-button'))).toBeTruthy();
  });

  it('should call handleReviewAction when approve button is clicked', () => {
    component.isHistoryTab = false;
    fixture.detectChanges();

    spyOn(component, 'handleReviewAction');
    const approveButton = fixture.debugElement.query(By.css('#approve-button'));
    approveButton.triggerEventHandler('click', null);

    expect(component.handleReviewAction).toHaveBeenCalledWith(mockFeedbacks[0], true);
  });

  it('should call handleReviewAction when reject button is clicked', () => {
    component.isHistoryTab = false;
    fixture.detectChanges();

    spyOn(component, 'handleReviewAction');
    const rejectButton = fixture.debugElement.query(By.css('#reject-button'));
    rejectButton.triggerEventHandler('click', null);

    expect(component.handleReviewAction).toHaveBeenCalledWith(mockFeedbacks[0], false);
  });
});
