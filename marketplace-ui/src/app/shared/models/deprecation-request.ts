import { PullRequestAction } from '../enums/pullrequest-action';

export interface DeprecationRequest {
  hasAlternativeExtension?: boolean;
  alternativeExtension?: string;
  successorUrl?: string;
  isDeprecated: boolean | null;
  isAddReadme?: boolean;
  pullRequestAction?: PullRequestAction;
  deprecationDate?: Date | null;
  deprecationRequester?: string;
}
