import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';
import { API_URI } from '../../shared/constants/api.constant';
@Injectable({
  providedIn: 'root'
})
export class ReleasePreviewService {
  private readonly http = inject(HttpClient);

  extractZipDetails(selectedFile: File): Observable<ReleasePreviewData> {
    const formData = new FormData();
    formData.append('file', selectedFile);

    return this.http.post<ReleasePreviewData>(
      API_URI.PREVIEW_RELEASE,
      formData
    );
  }
}
