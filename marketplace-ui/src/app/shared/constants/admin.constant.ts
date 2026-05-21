
import { SyncTaskRow } from '../models/sync-task-execution.model';

export const SYNC_TASK_KEYS = {
  SYNC_PRODUCTS: 'syncProducts',
  SYNC_ONE_PRODUCT: 'syncOneProduct',
  SYNC_ZIP_ARTIFACTS: 'syncZipArtifacts',
  SYNC_LATEST_RELEASES_FOR_PRODUCTS: 'syncLatestReleasesForProducts',
  SYNC_GITHUB_MONITOR: 'syncGithubMonitor',
  SYNC_GITHUB_SECURITY_MONITOR: 'syncGithubSecurityMonitor'
} as const;

export type SyncTaskKey = (typeof SYNC_TASK_KEYS)[keyof typeof SYNC_TASK_KEYS];

export const SYNC_TASKS: SyncTaskRow[] = [
  {
    key: SYNC_TASK_KEYS.SYNC_PRODUCTS,
    labelKey: 'common.admin.sync.tasks.syncProducts'
  },
  {
    key: SYNC_TASK_KEYS.SYNC_ONE_PRODUCT,
    labelKey: 'common.admin.sync.tasks.syncOneProduct'
  },
  {
    key: SYNC_TASK_KEYS.SYNC_ZIP_ARTIFACTS,
    labelKey: 'common.admin.sync.tasks.syncZipArtifacts'
  },
  {
    key: SYNC_TASK_KEYS.SYNC_LATEST_RELEASES_FOR_PRODUCTS,
    labelKey: 'common.admin.sync.tasks.syncLatestReleasesForProducts'
  },
  {
    key: SYNC_TASK_KEYS.SYNC_GITHUB_MONITOR,
    labelKey: 'common.admin.sync.tasks.syncGithubMonitor'
  },
  {
    key: SYNC_TASK_KEYS.SYNC_GITHUB_SECURITY_MONITOR,
    labelKey: 'common.admin.sync.tasks.syncGithubSecurityMonitor'
  }
];
