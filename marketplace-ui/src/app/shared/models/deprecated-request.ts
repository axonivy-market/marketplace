import { PullRequestAction } from '../enums/pullrequest-action';

export interface DeprecatedRequest {
  productId: string;
  successorUrl?: string;
  deprecated?: boolean;
  addReadme?: boolean;
  pullRequestAction?: PullRequestAction;
}
