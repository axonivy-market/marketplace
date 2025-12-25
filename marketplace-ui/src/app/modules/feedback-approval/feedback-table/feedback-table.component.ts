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
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { LoadingSpinnerComponent } from "../../../shared/components/loading-spinner/loading-spinner.component";
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';

@Component({
  selector: 'app-feedback-table',
  imports: [CommonModule, FormsModule, TranslateModule, MultilingualismPipe, LoadingSpinnerComponent],
  templateUrl: './feedback-table.component.html',
  styleUrls: ['./feedback-table.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class FeedbackTableComponent {
  protected LoadingComponentId = LoadingComponentId;
  @Input() feedbacks: Feedback[] = [];
  @Input() isHistoryTab = false;
  @Input() isLoading = false;
  @Output() reviewAction = new EventEmitter<{
    feedback: Feedback;
    approved: boolean;
  }>();

  languageService = inject(LanguageService);

  handleReviewAction(feedback: Feedback, approved: boolean) {
    this.reviewAction.emit({ feedback, approved });
  }
}
