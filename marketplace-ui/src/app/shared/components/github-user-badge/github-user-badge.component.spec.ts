import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GithubUserBadgeComponent } from './github-user-badge.component';

describe('GithubUserBadgeComponent', () => {
  let component: GithubUserBadgeComponent;
  let fixture: ComponentFixture<GithubUserBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GithubUserBadgeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GithubUserBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
