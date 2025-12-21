import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, Output, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';

@Component({
  selector: 'app-admin-token',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './admin-token.component.html',
  styleUrls: ['./admin-token.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminTokenContainerComponent {
  themeService = inject(ThemeService);
  @Input() errorMessage = '';
  @Output() submitToken = new EventEmitter<string>();

  token = '';

  onSubmit(): void {
    this.submitToken.emit(this.token);
  }
}
