import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonitoringRedirectComponent } from './monitor-dashboard.component';

describe('MonitoringRedirectComponent', () => {
  let component: MonitoringRedirectComponent;
  let fixture: ComponentFixture<MonitoringRedirectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MonitoringRedirectComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MonitoringRedirectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
