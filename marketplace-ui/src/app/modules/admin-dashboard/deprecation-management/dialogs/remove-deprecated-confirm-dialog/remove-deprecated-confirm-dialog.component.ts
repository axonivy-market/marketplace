import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-remove-deprecation-confirm-dialog',
  imports: [TranslateModule],
  templateUrl: './remove-deprecated-confirm-dialog.component.html',
  styleUrl: './remove-deprecated-confirm-dialog.component.scss'
})
export class RemoveDeprecatedConfirmDialogComponent {
  @Input() visible = false;
  @Input() isClosing = false;
  @Input() isRemoving = false;
  @Input() removedProductId = '';

  @Output() closeDialog = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
}
