import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'githubUrl',
  standalone: true
})
export class GithubUrlPipe implements PipeTransform {
  transform(githubUserName: string): string {
    return `https://github.com/${githubUserName}`;
  }
}
