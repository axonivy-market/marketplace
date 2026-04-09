import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { DeprecatedRequest } from '../../../../../shared/models/deprecated-request';

@Component({
  selector: 'app-deprecate-form-dialog',
  imports: [FormsModule, TranslateModule],
  templateUrl: './deprecation-form-dialog.component.html',
  styleUrl: './deprecation-form-dialog.component.scss'
})
export class DeprecationFormDialogComponent {
  @Input() visible = false;
  @Input() isClosing = false;
  @Input() isDeprecating = false;
  @Input() dropdownOpen = false;
  @Input() filteredProductIds: string[] = [];
  @Input() deprecatedRequest!: DeprecatedRequest;
  @Input() productId = '';
  @Input() validationErrors: { productId?: string; successorUrl?: string } = {};
  @Output() closeDialog = new EventEmitter<void>();
  @Output() submitForm = new EventEmitter<void>();
  @Output() openDropdown = new EventEmitter<void>();
  @Output() filterProducts = new EventEmitter<string>();
  @Output() selectProduct = new EventEmitter<string>();
  @Output() updateDropdownState = new EventEmitter<boolean>();
  @Output() readmeChecked = new EventEmitter<void>();

  onBlur(): void {
    this.updateDropdownState.emit(false);
  }
}
