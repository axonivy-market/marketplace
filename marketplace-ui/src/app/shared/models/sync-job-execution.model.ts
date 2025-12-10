import { SyncJobExecution } from '../../modules/admin-dashboard/admin-dashboard.service';

export interface SyncJobRow
  extends Omit<SyncJobExecution, 'triggeredAt' | 'completedAt'> {
  labelKey: string;
  triggeredAt?: Date;
  completedAt?: Date;
}
