import { PullRequestAction } from '../enums/pullrequest-action';

export interface DeprecatedRequest {
  successorUrl?: string;
  isDeprecated: boolean | null;
  addReadme?: boolean;
  pullRequestAction?: PullRequestAction;
  deprecationRequester?: string;
}
