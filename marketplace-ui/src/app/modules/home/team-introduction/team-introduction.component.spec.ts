import { TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { TeamIntroductionComponent } from './team-introduction.component';

describe('TeamIntroductionComponent', () => {
  let component: TeamIntroductionComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeamIntroductionComponent, TranslateModule.forRoot()]
    }).compileComponents();

    component = TestBed.createComponent(TeamIntroductionComponent).componentInstance;
  });

  it('should check the size after init', () => {
    expect(component.members.length).toBe(8);
  });

  it('should expose PO and team member data from assets', () => {
    expect(component.po).toBeTruthy();
    expect(component.po.name).toBeTruthy();
    expect(component.po.avatarName).toContain('.png');

    const firstMember = component.members[0];
    expect(firstMember).toBeTruthy();
    expect(firstMember.name).toBeTruthy();
    expect(firstMember.title).toBeTruthy();
    expect(firstMember.githubUserName).toBeTruthy();
    expect(firstMember.avatarName).toContain('.png');
  });
});

