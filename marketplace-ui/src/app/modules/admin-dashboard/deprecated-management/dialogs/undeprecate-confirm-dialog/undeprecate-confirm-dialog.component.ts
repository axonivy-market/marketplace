import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-undeprecate-confirm-dialog',
  imports: [TranslateModule],
  templateUrl: './undeprecate-confirm-dialog.component.html',
  styleUrl: './undeprecate-confirm-dialog.component.scss'
})
export class UndeprecateConfirmDialogComponent {
  @Input() visible = false;
  @Input() isClosing = false;
  @Input() isUndeprecating = false;
  @Input() undeprecateProductId = '';

  @Output() closeDialog = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
}
