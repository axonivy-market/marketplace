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

  members: TeamMember[] = [
    {
      name: 'Hoan Nguyen',
      title: 'Scrum Master',
      intro: 'Refactor Machine.',
      githubUserName: 'nqhoan-axonivy',
      quote: 'Delete half the code, double the features.',
      statusEmoji: '🗑️'
    },
    {
      name: 'Hung Pham',
      title: 'Senior Developer',
      intro: 'Version Bumper',
      githubUserName: 'phhung-axonivy',
      quote: 'Another version, another future.',
      statusEmoji: '📦'
    },
    {
      name: 'Dinh Nguyen',
      title: 'Developer',
      intro: 'Tool Pusher',
      githubUserName: 'ntqdinh-axonivy',
      quote: 'Another tool. Big dreams',
      statusEmoji: '🛠️'
    },
    {
      name: 'Hoang Nguyen',
      title: 'Developer',
      intro: 'Spacing Therapist',
      githubUserName: 'vhhoang-axonivy',
      quote: 'I don’t fix bugs. I heal margins.',
      statusEmoji: '📏'
    },
    {
      name: 'Phuc Nguyen',
      title: 'Developer',
      intro: 'Localization Engineer.',
      githubUserName: 'tvtphuc-axonivy',
      quote: 'Translation is my cardio.',
      statusEmoji: '🌍'
    },
    {
      name: 'Thuy Nguyen',
      title: 'Developer',
      intro: 'Admin Panel Architect.',
      githubUserName: 'nntthuy-axonivy',
      quote: 'I build tools for people who build tools.',
      statusEmoji: '🏗️'
    },
    {
      name: 'Quan Nguyen',
      title: 'Developer',
      intro: 'Mass PR engineer',
      githubUserName: 'quanpham-axonivy',
      quote: 'One script. Many notifications',
      statusEmoji: '📢'
    },
    {
      name: 'Huy Truong',
      title: 'Developer',
      intro: 'API master.',
      githubUserName: 'thxhuy-axonivy',
      quote: 'Connecting systems, one endpoint at a time.',
      statusEmoji: '🔌'
    }
  ];
}
