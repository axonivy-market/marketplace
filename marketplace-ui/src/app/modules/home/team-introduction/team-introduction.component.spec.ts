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

  it('should check the value after create', () => {
    const member = component['createMember']('John Doe', 'Developer', 'Code Writer', 'johndoe', 'I love coding', '💻');
    
    expect(member.name).toBe('John Doe');
    expect(member.title).toBe('Developer');
    expect(member.intro).toBe('Code Writer');
    expect(member.githubUserName).toBe('johndoe');
    expect(member.quote).toBe('I love coding');
    expect(member.statusEmoji).toBe('💻');
  });
});

