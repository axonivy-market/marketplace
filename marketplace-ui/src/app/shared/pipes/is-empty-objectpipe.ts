import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  standalone: true,
  name: 'isEmptyObjectPipe'
})
export class IsEmptyObjectPipe implements PipeTransform {
  transform(value: any): boolean {
    return value && typeof value === 'object' && !Array.isArray(value)
      ? Object.keys(value).length === 0
      : false;
  }
}