export interface ScheduledTaskInfo {
  id: string;
  cronExpression?: string | null;
  lastStart?: string | null;
  lastEnd?: string | null;
  lastSuccessEnd?: string | null;
  nextExecution?: string | null;
  running: boolean;
  lastSuccess: boolean;
  lastError?: string | null;
}
