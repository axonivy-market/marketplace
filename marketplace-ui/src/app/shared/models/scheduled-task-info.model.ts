export interface ScheduledTaskInfo {
  name: string;
  cronExpression?: string | null;
  lastStart: Date;
  lastEnd: Date;
  status: 'RUNNING' | 'SUCCESS' | 'FAILED';
  nextExecution?: Date;
  running: boolean;
}