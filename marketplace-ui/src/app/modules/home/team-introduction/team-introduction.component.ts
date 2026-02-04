import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface TeamMember {
  name: string;
  title: string;
  intro: string;
  imageUrl: string;
  linkedin: string;
  github: string;
  quote?: string;
  statusEmoji?: string;
}

@Component({
  selector: 'app-team-introduction',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './team-introduction.component.html',
  styleUrls: ['./team-introduction.component.scss']
})
export class TeamIntroductionComponent {
  readonly teamDescription =
    'As the core engineering unit behind the Axon Ivy Marketplace, we drive ecosystem innovation and stability. We specialize in the end-to-end lifecycle management of platform extensions, delivering new integration solutions, ensuring seamless version compatibility, and enforcing rigorous standards for security compliance and runtime performance.';

  po: TeamMember = {
    name: 'Dr. Sabine Gillner',
    title: 'Senior Product Owner & Team Lead',
    intro:
      'Dr. Gillner is an experienced Team Manager and Product Owner specializing in Digitalization and Process Orchestration. With a background in Cognitive Neuroscience and over a decade of leadership in tech and automotive sectors, she brings a unique perspective to innovation and AI.',
    imageUrl: 'https://avatars.githubusercontent.com/u/129939502?v=4',
    linkedin: 'https://www.linkedin.com/in/sabinegillner/',
    github: 'https://github.com/sabinegillner',
    quote:
      'Innovation starts with understanding the human mind and empowering teams to build intelligent solutions.',
    statusEmoji: '🧠'
  };

  members: TeamMember[] = [
    {
      name: 'Hoan Nguyen',
      title: 'Scrum Master',
      intro: 'Refactor Machine.',
      imageUrl: 'https://i.pravatar.cc/300?img=5',
      linkedin: 'https://linkedin.com/in/sarahmiller-placeholder',
      github: 'https://github.com/sarahmiller',
      quote: 'Delete half the code, double the features.',
      statusEmoji: '🗑️'
    },
    {
      name: 'Hung Pham',
      title: 'Senior Developer',
      intro: 'Version Bumper',
      imageUrl: 'https://i.pravatar.cc/300?img=3',
      linkedin: 'https://linkedin.com/in/jameswilson-placeholder',
      github: 'https://github.com/jameswilson',
      quote: 'Another version, another future.',
      statusEmoji: '📦'
    },
    {
      name: 'Dinh Nguyen',
      title: 'Developer',
      intro: 'Tool Pusher',
      imageUrl: 'https://i.pravatar.cc/300?img=9',
      linkedin: 'https://linkedin.com/in/mariagarcia-placeholder',
      github: 'https://github.com/mariagarcia',
      quote: 'Another tool. Big dreams',
      statusEmoji: '🛠️'
    },
    {
      name: 'Hoang Nguyen',
      title: 'Developer',
      intro: 'Spacing Therapist',
      imageUrl: 'https://i.pravatar.cc/300?img=8',
      linkedin: 'https://linkedin.com/in/davidlee-placeholder',
      github: 'https://github.com/davidlee',
      quote: 'I don’t fix bugs. I heal margins.',
      statusEmoji: '📏'
    },
    {
      name: 'Phuc Nguyen',
      title: 'Developer',
      intro: 'Localization Engineer.',
      imageUrl: 'https://i.pravatar.cc/300?img=1',
      linkedin: 'https://linkedin.com/in/emmadavis-placeholder',
      github: 'https://github.com/emmadavis',
      quote: 'Translation is my cardio.',
      statusEmoji: '🌍'
    },
    {
      name: 'Thuy Nguyen',
      title: 'Developer',
      intro: 'Admin Panel Architect.',
      imageUrl: 'https://i.pravatar.cc/300?img=4',
      linkedin: 'https://linkedin.com/in/michaelchen-placeholder',
      github: 'https://github.com/michaelchen',
      quote: 'I build tools for people who build tools.',
      statusEmoji: '🏗️'
    },
    {
      name: 'Quan Nguyen',
      title: 'Developer',
      intro: 'Mass PR engineer',
      imageUrl: 'https://i.pravatar.cc/300?img=20',
      linkedin: 'https://linkedin.com/in/lisataylor-placeholder',
      github: 'https://github.com/lisataylor',
      quote: 'One script. Many notifications',
      statusEmoji: '📢'
    },
    {
      name: 'Huy Truong',
      title: 'Developer',
      intro: 'API master.',
      imageUrl: 'https://i.pravatar.cc/300?img=13',
      linkedin: 'https://linkedin.com/in/robertbrown-placeholder',
      github: 'https://github.com/robertbrown',
      quote: 'Connecting systems, one endpoint at a time.',
      statusEmoji: '🔌'
    }
  ];
}
