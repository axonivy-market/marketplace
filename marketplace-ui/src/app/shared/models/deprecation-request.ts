import { PullRequestAction } from '../enums/pullrequest-action';

export interface DeprecationRequest {
  successorUrl?: string;
  isDeprecated: boolean | null;
  isAddReadme?: boolean;
  pullRequestAction?: PullRequestAction;
  deprecationDate?: Date | null;
  deprecationRequester?: string;
}
