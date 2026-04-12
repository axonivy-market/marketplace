import { PullRequestAction } from '../enums/pullrequest-action';

export interface DeprecationRequest {
  successorUrl?: string;
  isDeprecated: Boolean | null;
  isAddReadme?: boolean;
  pullRequestAction?: PullRequestAction;
  deprecationRequester?: string;
}
