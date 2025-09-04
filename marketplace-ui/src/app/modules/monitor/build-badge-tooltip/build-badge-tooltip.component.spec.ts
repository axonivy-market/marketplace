import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BuildBadgeTooltipComponent } from './build-badge-tooltip.component';
import { TranslateService } from '@ngx-translate/core';
import { CI_BUILD, DEV_BUILD, E2E_BUILD } from '../../../shared/constants/common.constant';
import { Subject } from 'rxjs';

describe('BuildBadgeTooltipComponent', () => {
  let component: BuildBadgeTooltipComponent;
  let fixture: ComponentFixture<BuildBadgeTooltipComponent>;
  let mockTranslateService: jasmine.SpyObj<TranslateService>;

  beforeEach(async () => {
    const langChangeSubject = new Subject<any>();
    mockTranslateService = jasmine.createSpyObj('TranslateService', ['instant'], {
      onLangChange: langChangeSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      imports: [BuildBadgeTooltipComponent],
      providers: [{ provide: TranslateService, useValue: mockTranslateService }]
    }).compileComponents();

    fixture = TestBed.createComponent(BuildBadgeTooltipComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    mockTranslateService.instant.calls.reset();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should call constructToolTipContent on ngOnInit', () => {
    spyOn(component, 'constructToolTipContent');

    component.ngOnInit();

    expect(component.constructToolTipContent).toHaveBeenCalled();
  });

  it('should set tooltipContent for CI build type', () => {
    const mockTranslation = 'CI Build Tooltip';
    mockTranslateService.instant.and.returnValue(mockTranslation);

    component.buildType = CI_BUILD;
    component.constructToolTipContent();

    expect(mockTranslateService.instant).toHaveBeenCalledWith(
      'common.monitor.buildTooltip.ci'
    );
    expect(component.tooltipContent).toBe(mockTranslation);
  });

  it('should set tooltipContent for DEV build type', () => {
    const mockTranslation = 'DEV Build Tooltip';
    mockTranslateService.instant.and.returnValue(mockTranslation);

    component.buildType = DEV_BUILD;
    component.constructToolTipContent();

    expect(mockTranslateService.instant).toHaveBeenCalledWith(
      'common.monitor.buildTooltip.dev'
    );
    expect(component.tooltipContent).toBe(mockTranslation);
  });

  it('should set tooltipContent for E2E build type', () => {
    const mockTranslation = 'E2E Build Tooltip';
    mockTranslateService.instant.and.returnValue(mockTranslation);

    component.buildType = E2E_BUILD;
    component.constructToolTipContent();

    expect(mockTranslateService.instant).toHaveBeenCalledWith(
      'common.monitor.buildTooltip.e2e'
    );
    expect(component.tooltipContent).toBe(mockTranslation);
  });
});
