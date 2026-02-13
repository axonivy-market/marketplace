import { SafeHtml } from "@angular/platform-browser";

export interface ReleaseLetterSafeHtml {
  sprint: string;
  content: SafeHtml;
  latest: boolean;
  createdAt: string;
}
