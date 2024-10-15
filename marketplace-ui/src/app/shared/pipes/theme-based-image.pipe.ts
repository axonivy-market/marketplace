import { Pipe, PipeTransform } from '@angular/core';
import { ThemeService } from '../../core/services/theme/theme.service';

@Pipe({
  name: 'themeBasedImage',
  standalone: true
})
export class ThemeBasedImagePipe implements PipeTransform {

  constructor(private themeService: ThemeService) {}

  transform(lightImage: string, darkImage: string): string {
    return this.themeService.isDarkMode() ? darkImage : lightImage;
  }

}
