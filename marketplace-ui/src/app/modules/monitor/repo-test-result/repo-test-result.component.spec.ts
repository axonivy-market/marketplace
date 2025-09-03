import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RepoTestResultComponent } from './repo-test-result.component';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';

describe('RepoTestResultComponent', () => {
  let component: RepoTestResultComponent;
  let fixture: ComponentFixture<RepoTestResultComponent>;
  let mockTranslateService: jasmine.SpyObj<TranslateService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockTranslateService = jasmine.createSpyObj('TranslateService', [
      'instant'
    ]);

    await TestBed.configureTestingModule({
      imports: [RepoTestResultComponent, MatomoTestingModule.forRoot()],
      providers: [{ provide: TranslateService, useValue: mockTranslateService }]
    }).compileComponents();

    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
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

  it('should navigate to report page on badge click', () => {
    const repoName = 'test-repo';
    const workflow = 'ci';

    component.onBadgeClick(repoName, workflow, 'report');

    expect(router.navigate).toHaveBeenCalledWith(['/monitoring', repoName, 'CI']);
  });

});
