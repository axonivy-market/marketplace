import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';
import { API_URI } from '../../shared/constants/api.constant';
import { Feedback } from '../../shared/models/feedback.model';
@Injectable({
  providedIn: 'root'
})
export class FeedbackApprovalService {
  private readonly http = inject(HttpClient);

//   getAllFeedbacks(selectedFile: File): Observable<Feedback> {
//     const formData = new FormData();
//     formData.append('file', selectedFile);

//     return this.http.post<Feedback>(
//       API_URI.FEEDBACK_APPROVAL,
//       formData
//     );
//   }
}