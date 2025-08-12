import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'workflowIcon',
  standalone: true,
})
export class WorkflowIconPipe implements PipeTransform {
  transform(workflow: string): string {
    const icons: Record<string, string> = {
      CI: '🏷️',
      DEV: '🛠️',
      E2E: '♻️'
    };

    return icons[workflow] || '🧹';
  }
}
