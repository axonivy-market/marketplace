import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FeedbackApprovalComponent } from "./feedback-approval.component";
import { ProductFeedbackService } from "../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { CommonModule } from "@angular/common";
import { AuthService } from "../../auth/auth.service";
import { By } from "@angular/platform-browser";
import { ThemeService } from "../../core/services/theme/theme.service";
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from "@angular/core";

describe('FeedbackApprovalComponent', () => {
let component: FeedbackApprovalComponent;
  let fixture: ComponentFixture<FeedbackApprovalComponent>;
  let productFeedbackService: ProductFeedbackService;
  let translateService: TranslateService;
  let authService: jasmine.SpyObj<AuthService>;
  let themeService: jasmine.SpyObj<ThemeService>;
  let mockProductFeedbackService: jasmine.SpyObj<ProductFeedbackService>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['getToken', 'redirectToGitHub']);
    const themeSpy = jasmine.createSpyObj('ThemeService', ['isDarkMode']);
    mockProductFeedbackService = jasmine.createSpyObj(
          'ProductFeedbackService',
          [
            'getInitFeedbacksObservable',
            'findProductFeedbacksByCriteria',
            'handleFeedbackApiResponse',
            'findProductFeedbackOfUser',
            'totalElements'
          ],
          {
            feedbacks: signal([]),
            totalElements: signal(0)
          }
        );
    await TestBed.configureTestingModule({
      imports: [ FeedbackApprovalComponent, CommonModule, TranslateModule.forRoot() ],
      providers: [
        ProductFeedbackService,
        TranslateService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authSpy },
        { provide: ThemeService, useValue: themeSpy }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedbackApprovalComponent);
    component = fixture.componentInstance;
    productFeedbackService = TestBed.inject(ProductFeedbackService);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    themeService = TestBed.inject(ThemeService) as jasmine.SpyObj<ThemeService>;
    spyOn(productFeedbackService, 'changeSort').and.callThrough();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render when authenticated', () => {
    fixture.detectChanges();
    const container = fixture.debugElement.query(By.css('.container'));
    expect(container).toBeTruthy();
  });

  it('should not render when not authenticated', () => {
    authService.getToken.and.returnValue(null);
    fixture.detectChanges();
    const container = fixture.debugElement.query(By.css('.container'));
    expect(container.children.length).toBe(0);
  });

  it('should display tabs correctly', () => {
    fixture.detectChanges();
    const tabs = fixture.debugElement.queryAll(By.css('.nav-tabs .nav-item'));
    expect(tabs.length).toBe(2);
    
    const reviewTab = tabs[0].query(By.css('a'));
    const historyTab = tabs[1].query(By.css('a'));
    
    expect(reviewTab.nativeElement.textContent.trim()).toBe('Review Feedback');
    expect(historyTab.nativeElement.textContent.trim()).toBe('History');
  });

  it('should switch tabs when clicked', () => {
    fixture.detectChanges();
    
    // Initially review tab should be active
    expect(component.activeTab).toBe('review');
    
    // Click history tab
    const historyTab = fixture.debugElement.query(By.css('#history-tab'));
    historyTab.triggerEventHandler('click', null);
    fixture.detectChanges();
    
    expect(component.activeTab).toBe('history');
    const historyContent = fixture.debugElement.query(By.css('.tab-pane.show.active'));
    expect(historyContent).toBeTruthy();
  });

  it('should display pending feedbacks table', () => {
    fixture.detectChanges();
    const tableRows = fixture.debugElement.queryAll(By.css('.table-responsive:first-child tbody tr'));
    expect(tableRows.length).toBe(1);
    
    const cells = tableRows[0].queryAll(By.css('td'));
    expect(cells.length).toBe(7);
    expect(cells[0].query(By.css('.feedback-username')).nativeElement.textContent.trim()).toBe('testUser');
  });

  it('should trigger approve button click', () => {
    spyOn(component, 'onClickingApproveButton');
    fixture.detectChanges();
    
    const approveButton = fixture.debugElement.query(By.css('#approve-button'));
    approveButton.triggerEventHandler('click', null);
    expect(component.onClickingApproveButton).toHaveBeenCalled();
  });

  it('should trigger reject button click', () => {
    spyOn(component, 'onClickingRejectButton');
    fixture.detectChanges();
    
    const rejectButton = fixture.debugElement.query(By.css('#reject-button'));
    rejectButton.triggerEventHandler('click', null);
    expect(component.onClickingRejectButton).toHaveBeenCalled();
  });
});