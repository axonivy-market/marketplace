import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AvatarUrlPipe } from '../../../shared/pipes/avatar-url.pipe';
import { GithubUrlPipe } from '../../../shared/pipes/github-url.pipe';

interface TeamMember {
  name: string;
  title: string;
  intro: string;
  githubUserName: string;
  quote?: string;
  statusEmoji?: string;
}

@Component({
  selector: 'app-team-introduction',
  standalone: true,
  imports: [CommonModule, TranslateModule, AvatarUrlPipe, GithubUrlPipe],
  templateUrl: './team-introduction.component.html',
  styleUrls: ['./team-introduction.component.scss']
})
export class TeamIntroductionComponent {
  translateService = inject(TranslateService);
  members: TeamMember[] = this.initializeTeamMembers();

  private initializeTeamMembers(): TeamMember[] {
    return [
      this.createMember('Hoan Nguyen', 'Scrum Master', 'Refactor Machine.', 'nqhoan-axonivy', 'Delete half the code, double the features.', '🗑️'),
      this.createMember('Hung Pham', 'Senior Developer', 'Version Bumper', 'phhung-axonivy', 'Another version, another future.', '📦'),
      this.createMember('Dinh Nguyen', 'Developer', 'Tool Pusher', 'ntqdinh-axonivy', 'Another tool. Big dreams', '🛠️'),
      this.createMember('Hoang Nguyen', 'Developer', 'Spacing Therapist', 'vhhoang-axonivy', 'I don\'t fix bugs. I heal margins.', '📏'),
      this.createMember('Phuc Nguyen', 'Developer', 'Localization Engineer.', 'tvtphuc-axonivy', 'Translation is my cardio.', '🌍'),
      this.createMember('Thuy Nguyen', 'Developer', 'Admin Panel Architect.', 'nntthuy-axonivy', 'I build tools for people who build tools.', '🏗️'),
      this.createMember('Quan Nguyen', 'Developer', 'Mass PR engineer', 'quanpham-axonivy', 'One script. Many notifications', '📢'),
      this.createMember('Huy Truong', 'Developer', 'API master.', 'thxhuy-axonivy', 'Connecting systems, one endpoint at a time.', '🔌')
    ];
  }

  private createMember(name: string, title: string, intro: string, githubUserName: string, quote: string, statusEmoji: string): TeamMember {
    return { name, title, intro, githubUserName, quote, statusEmoji };
  }
}
