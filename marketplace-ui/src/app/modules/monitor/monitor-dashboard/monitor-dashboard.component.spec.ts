import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonitoringDashboardComponent } from './monitor-dashboard.component';

describe('MonitoringDashboardComponent', () => {
  let component: MonitoringDashboardComponent;
  let fixture: ComponentFixture<MonitoringDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MonitoringDashboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MonitoringDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
