import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  standalone: true,
  name: 'isEmptyObjectPipe'
})
export class IsEmptyObjectPipe implements PipeTransform {
  transform(value: unknown): boolean {
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      return Object.keys(value).length === 0;
    }
    return false;
  }
}