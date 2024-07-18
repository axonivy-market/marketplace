import { Component, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../../../../../auth/auth.service';
import { NgOptimizedImage } from '@angular/common';

@Component({
  selector: 'app-success-dialog',
  standalone: true,
  imports: [TranslateModule, NgOptimizedImage],
  templateUrl: './success-dialog.component.html',
  styleUrls: ['./success-dialog.component.scss']
})
export class SuccessDialogComponent {

  activeModal = inject(NgbActiveModal);

  authService = inject(AuthService);
}