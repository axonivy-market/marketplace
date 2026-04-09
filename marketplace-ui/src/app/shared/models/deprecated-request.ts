import { PullRequestAction } from '../enums/pullrequest-action';

export interface DeprecatedRequest {
  successorUrl?: string;
  isDeprecated: Boolean | null;
  addReadme?: boolean;
  pullRequestAction?: PullRequestAction;
  deprecationRequester?: string;
}
