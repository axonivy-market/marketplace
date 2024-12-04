import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityMonitorComponent } from './security-monitor.component';

describe('SecurityMonitorComponent', () => {
  let component: SecurityMonitorComponent;
  let fixture: ComponentFixture<SecurityMonitorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityMonitorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecurityMonitorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
