import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  inject,
  Input,
  Output,
  ViewEncapsulation
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { Feedback } from '../../../shared/models/feedback.model';

@Component({
  selector: 'app-feedback-table',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './feedback-table.component.html',
  styleUrls: ['./feedback-table.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class FeedbackTableComponent {
  @Input() feedbacks: Feedback[] = [];
  @Input() isHistoryTab = false;
  @Output() reviewAction = new EventEmitter<{
    feedback: Feedback;
    approved: boolean;
  }>();

  languageService = inject(LanguageService);

  handleReviewAction(feedback: Feedback, approved: boolean) {
    this.reviewAction.emit({ feedback, approved });
  }
}
