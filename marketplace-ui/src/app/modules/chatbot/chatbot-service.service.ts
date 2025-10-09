import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { API_URI } from '../../shared/constants/api.constant';

interface ChatbotResponse {
  message: string;
  sessionId: string | null;
  success: boolean;
  error: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotServiceService {
  constructor(private readonly http: HttpClient) { }
  getChatbotResponse(message: string) {
    const url = `${API_URI.CHATBOT}`;
    return this.http.post<ChatbotResponse>(url, { message });
  }
}
