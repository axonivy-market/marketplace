import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RepoReportComponent } from './repo-report.component';

describe('RepoReportComponent', () => {
  let component: RepoReportComponent;
  let fixture: ComponentFixture<RepoReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RepoReportComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RepoReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
