import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-remove-deprecation-confirm-dialog',
  imports: [TranslateModule],
  templateUrl: './remove-deprecated-confirm-dialog.component.html',
  styleUrl: './remove-deprecated-confirm-dialog.component.scss'
})
export class removeDeprecatedConfirmDialogComponent {
  @Input() visible = false;
  @Input() isClosing = false;
  @Input() isUndeprecating = false;
  @Input() undeprecateProductId = '';

  @Output() closeDialog = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
}
