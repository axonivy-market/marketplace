import { PullRequestAction } from '../enums/pullrequest-action';

export interface DeprecatedRequest {
  productId: string;
  successorUrl?: string;
  deprecated: boolean | null;
  addReadme?: boolean;
  pullRequestAction?: PullRequestAction;
  deprecationRequester?: string;
}
