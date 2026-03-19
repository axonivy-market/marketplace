import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'avatarUrl',
  standalone: true
})
export class AvatarUrlPipe implements PipeTransform {
  transform(imageName: string | number): string {
    return `/assets/team/avatar/${imageName}`;
  }
}
