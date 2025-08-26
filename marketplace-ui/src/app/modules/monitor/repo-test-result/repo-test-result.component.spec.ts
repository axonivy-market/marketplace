import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RepoTestResultComponent } from './repo-test-result.component';
import { TranslateService } from '@ngx-translate/core';
import { CI_BUILD, DEV_BUILD, E2E_BUILD } from '../../../shared/constants/common.constant';

describe('BuildBadgeTooltipComponent', () => {
  let component: RepoTestResultComponent;
  let fixture: ComponentFixture<RepoTestResultComponent>;
  let mockTranslateService: jasmine.SpyObj<TranslateService>;

  beforeEach(async () => {
    mockTranslateService = jasmine.createSpyObj('TranslateService', [
      'instant'
    ]);

    await TestBed.configureTestingModule({
      imports: [RepoTestResultComponent],
      providers: [{ provide: TranslateService, useValue: mockTranslateService }]
    }).compileComponents();

    fixture = TestBed.createComponent(RepoTestResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    mockTranslateService.instant.calls.reset();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
