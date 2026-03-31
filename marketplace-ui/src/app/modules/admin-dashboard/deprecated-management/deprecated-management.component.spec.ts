import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeprecatedManagementComponent } from './deprecated-management.component';

describe('DeprecatedManagementComponent', () => {
  let component: DeprecatedManagementComponent;
  let fixture: ComponentFixture<DeprecatedManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeprecatedManagementComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DeprecatedManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
