import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AvatarUrlPipe } from '../../../shared/pipes/avatar-url.pipe';
import { GithubUrlPipe } from '../../../shared/pipes/github-url.pipe';
import teamMembersData from '../../../../assets/team/team-members.json';

interface TeamMember {
  name: string;
  title: string;
  intro: string;
  githubUserName: string;
  avatarName: string;
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
  members: TeamMember[] = teamMembersData.members;
  po: TeamMember = teamMembersData.po;
}
