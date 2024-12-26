import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';
@Injectable({
  providedIn: 'root'
})
export class ReleasePreviewService {
  private readonly apiUrl = environment.apiUrl + '/api/release-preview';
  private readonly http = inject(HttpClient);

  extractZipDetails(selectedFile: File): Observable<ReleasePreviewData> {
    const formData = new FormData();
    formData.append('file', selectedFile);

    return this.http.post<ReleasePreviewData>(this.apiUrl, formData);
  }
}
