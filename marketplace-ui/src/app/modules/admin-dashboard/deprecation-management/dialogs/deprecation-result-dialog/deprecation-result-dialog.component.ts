import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-deprecation-result-dialog',
  imports: [TranslateModule],
  templateUrl: './deprecation-result-dialog.component.html',
  styleUrl: './deprecation-result-dialog.component.scss'
})
export class DeprecationResultDialogComponent {
  @Input() visible = false;
  @Input() isClosing = false;
  @Input() successMode: 'deprecate' | 'undeprecate' | null = null;
  @Input() moderatorName: string | undefined;
  @Input() pullRequestUrl: string | null = null;
  @Input() isCopySuccessVisible = false;
  @Input() showPullRequest = false;

  @Output() closeDialog = new EventEmitter<void>();
  @Output() copyPullRequestUrl = new EventEmitter<void>();
}
