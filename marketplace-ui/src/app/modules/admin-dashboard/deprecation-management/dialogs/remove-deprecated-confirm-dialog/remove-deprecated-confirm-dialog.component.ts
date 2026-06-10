import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgClass } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-remove-deprecation-confirm-dialog',
  imports: [TranslateModule, NgClass],
  templateUrl: './remove-deprecated-confirm-dialog.component.html',
  styleUrl: './remove-deprecated-confirm-dialog.component.scss'
})
export class RemoveDeprecatedConfirmDialogComponent {
  @Input() visible = false;
  @Input() isClosing = false;
  @Input() isRemoving = false;
  @Input() removedProductId = '';
  @Input() titleKey = 'common.admin.deprecation.confirmRemoveDeprecatedTitle';
  @Input() contentKey = 'common.admin.deprecation.confirmRemoveDeprecatedContent';
  @Input() confirmButtonKey = 'common.admin.deprecation.removeDeprecated';
  @Input() confirmButtonClass = 'btn-danger';

  @Output() closeDialog = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
}
