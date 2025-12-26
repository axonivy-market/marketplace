import { SyncTaskExecution } from '../../modules/admin-dashboard/admin-dashboard.service';

export interface SyncTaskRow
  extends Omit<SyncTaskExecution, 'triggeredAt' | 'completedAt'> {
  labelKey: string;
  triggeredAt?: Date;
  completedAt?: Date;
}
