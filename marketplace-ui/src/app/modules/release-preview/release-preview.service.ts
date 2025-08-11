import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';
import { API_URI } from '../../shared/constants/api.constant';
import { LoadingComponent } from '../../core/interceptors/api.interceptor';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
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
      formData,
      {
        context: new HttpContext().set(
          LoadingComponent,
          LoadingComponentId.RELEASE_PREVIEW
        )
      }
    );
  }
}
