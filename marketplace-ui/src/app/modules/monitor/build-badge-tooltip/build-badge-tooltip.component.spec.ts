import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BuildBadgeTooltipComponent } from './build-badge-tooltip.component';

describe('BuildBadgeTooltipComponent', () => {
  let component: BuildBadgeTooltipComponent;
  let fixture: ComponentFixture<BuildBadgeTooltipComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BuildBadgeTooltipComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BuildBadgeTooltipComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
