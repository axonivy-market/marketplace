import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-deprecate-success-dialog',
  imports: [TranslateModule],
  templateUrl: './deprecate-success-dialog.component.html',
  styleUrl: './deprecate-success-dialog.component.scss'
})
export class DeprecateSuccessDialogComponent {
  @Input() visible = false;
  @Input() isClosing = false;
  @Input() successMode: 'deprecate' | 'undeprecate' | null = null;
  @Input() moderatorName: string | undefined;
  @Input() pullRequestUrl: string | null = null;
  @Input() isCopySuccessVisible = false;
  @Input() showPullRequest = false;

  @Output() close = new EventEmitter<void>();
  @Output() copy = new EventEmitter<void>();
}
