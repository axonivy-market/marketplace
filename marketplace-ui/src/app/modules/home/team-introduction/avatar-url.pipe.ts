import { Pipe, PipeTransform, inject } from '@angular/core';
import { environment } from '../../../../../environments/environment';

@Pipe({
  name: 'avatarUrl',
  standalone: true
})
export class AvatarUrlPipe implements PipeTransform {
  transform(imageId: string | number): string {
    return `${environment.apiUrl}/api/image/${imageId}`;
  }
}
