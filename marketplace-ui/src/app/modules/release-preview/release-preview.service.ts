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

  getMockResponse(): Observable<ReleasePreviewData> {
    return of({
      description: {
        English: 'This is a description in English.',
        Spanish: 'Esta es una descripción en español.',
        French: 'Ceci est une description en français.'
      },
      setup: {
        English: 'To set up the application, follow these steps...',
        Spanish: 'Para configurar la aplicación, siga estos pasos...',
        French: "Pour configurer l'application, suivez ces étapes..."
      },
      demo: {
        English: 'To demo the app, use the following commands...',
        Spanish: 'Para mostrar la aplicación, use los siguientes comandos...',
        French:
          "Pour démontrer l'application, utilisez les commandes suivantes..."
      }
    });
  }
}
