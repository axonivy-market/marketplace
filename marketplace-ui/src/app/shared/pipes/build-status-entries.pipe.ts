import { Pipe, PipeTransform } from '@angular/core';

export interface TestSummary {
  PASSED?: number;
  FAILED?: number;
  SKIPPED?: number;
}

@Pipe({
  name: 'buildStatusEntries',
  standalone: true,
})
export class BuildStatusEntriesPipe implements PipeTransform {
  transform(results?: TestSummary): { label: string; icon: string; count: number }[] {
    if (!results) {
      return [];
    }

    const statusMap: Record<keyof TestSummary, { label: string; icon: string }> = {
      PASSED: { label: 'monitor.dashboard.passed', icon: '✅' },
      FAILED: { label: 'monitor.dashboard.failed', icon: '❌' },
      SKIPPED: { label: 'monitor.dashboard.skipped', icon: '⏩' },
    };

    return (Object.keys(statusMap) as (keyof TestSummary)[])
      .filter(key => results[key])
      .map(key => ({
        label: statusMap[key].label,
        icon: statusMap[key].icon,
        count: results[key] ?? 0
      }));
  }
}
