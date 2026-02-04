import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

interface TeamMember {
  name: string;
  title: string;
  intro: string;
  imageUrl: string;
  github: string;
  quote?: string;
  statusEmoji?: string;
}

@Component({
  selector: 'app-team-introduction',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './team-introduction.component.html',
  styleUrls: ['./team-introduction.component.scss']
})
export class TeamIntroductionComponent implements OnInit {
  teamTitle: string = '';
  teamDescription: string = '';
  translateService = inject(TranslateService);


  ngOnInit() {
    this.translateService.get('common.team.title').subscribe(res => {
      this.teamTitle = res;
    });
    this.translateService.get('common.team.description').subscribe(res => {
      this.teamDescription = res;
    });
  }

  members: TeamMember[] = [
    {
      name: 'Hoan Nguyen',
      title: 'Scrum Master',
      intro: 'Refactor Machine.',
      imageUrl: 'https://i.pravatar.cc/300?img=5',
      github: 'https://github.com/sarahmiller',
      quote: 'Delete half the code, double the features.',
      statusEmoji: '🗑️'
    },
    {
      name: 'Hung Pham',
      title: 'Senior Developer',
      intro: 'Version Bumper',
      imageUrl: 'https://i.pravatar.cc/300?img=3',
      github: 'https://github.com/jameswilson',
      quote: 'Another version, another future.',
      statusEmoji: '📦'
    },
    {
      name: 'Dinh Nguyen',
      title: 'Developer',
      intro: 'Tool Pusher',
      imageUrl: 'https://i.pravatar.cc/300?img=9',
      github: 'https://github.com/mariagarcia',
      quote: 'Another tool. Big dreams',
      statusEmoji: '🛠️'
    },
    {
      name: 'Hoang Nguyen',
      title: 'Developer',
      intro: 'Spacing Therapist',
      imageUrl: 'https://i.pravatar.cc/300?img=8',
      github: 'https://github.com/davidlee',
      quote: 'I don’t fix bugs. I heal margins.',
      statusEmoji: '📏'
    },
    {
      name: 'Phuc Nguyen',
      title: 'Developer',
      intro: 'Localization Engineer.',
      imageUrl: 'https://i.pravatar.cc/300?img=1',
      github: 'https://github.com/emmadavis',
      quote: 'Translation is my cardio.',
      statusEmoji: '🌍'
    },
    {
      name: 'Thuy Nguyen',
      title: 'Developer',
      intro: 'Admin Panel Architect.',
      imageUrl: 'https://i.pravatar.cc/300?img=4',
      github: 'https://github.com/michaelchen',
      quote: 'I build tools for people who build tools.',
      statusEmoji: '🏗️'
    },
    {
      name: 'Quan Nguyen',
      title: 'Developer',
      intro: 'Mass PR engineer',
      imageUrl: 'https://i.pravatar.cc/300?img=20',
      github: 'https://github.com/lisataylor',
      quote: 'One script. Many notifications',
      statusEmoji: '📢'
    },
    {
      name: 'Huy Truong',
      title: 'Developer',
      intro: 'API master.',
      imageUrl: 'https://i.pravatar.cc/300?img=13',
      github: 'https://github.com/robertbrown',
      quote: 'Connecting systems, one endpoint at a time.',
      statusEmoji: '🔌'
    }
  ];
}
