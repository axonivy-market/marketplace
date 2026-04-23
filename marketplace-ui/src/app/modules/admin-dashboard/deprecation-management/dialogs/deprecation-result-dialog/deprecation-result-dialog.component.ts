import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { NgOptimizedImage } from '@angular/common';
import { DeprecationMode } from '../../../../../shared/enums/deprecation-mode.enum';

@Component({
  selector: 'app-deprecation-result-dialog',
  imports: [TranslateModule, NgOptimizedImage],
  templateUrl: './deprecation-result-dialog.component.html',
  styleUrl: './deprecation-result-dialog.component.scss'
})
export class DeprecationResultDialogComponent {
  readonly DeprecationMode = DeprecationMode;

  @Input() visible = false;
  @Input() isClosing = false;
  @Input() successMode: DeprecationMode | null = null;
  @Input() moderatorName: string | undefined;
  @Input() pullRequestUrl: string | null = null;
  @Input() isCopySuccessVisible = false;
  @Input() showPullRequest = false;

  @Output() closeDialog = new EventEmitter<void>();
  @Output() copyPullRequestUrl = new EventEmitter<void>();
}
