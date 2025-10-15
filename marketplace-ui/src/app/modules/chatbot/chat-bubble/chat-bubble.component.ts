import { Component, ViewChild, ElementRef, AfterViewChecked, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotServiceService } from '../chatbot-service.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import MarkdownIt from 'markdown-it';
import { TranslateService, TranslateModule } from '@ngx-translate/core';

interface ChatMessage {
  message: string;
  isUser: boolean;
  timestamp: Date;
}

interface ChatbotResponse {
  message: string;
  sessionId: string | null;
  success: boolean;
  error: string | null;
}

@Component({
  selector: 'app-chat-bubble',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './chat-bubble.component.html',
  styleUrls: ['./chat-bubble.component.scss']
})
export class ChatBubbleComponent implements AfterViewChecked {
  @ViewChild('chatMessages') private chatMessagesContainer!: ElementRef;
  translateService = inject(TranslateService);
  isExpanded = false;
  currentMessage = '';
  messages: ChatMessage[] = [];
  isLoading = false;
  private isInitialized = false;
  private markdownParser = new MarkdownIt({
    linkify: true 
  });

  constructor(private chatbotService: ChatbotServiceService, private sanitizer: DomSanitizer) {
    this.markdownParser.renderer.rules['link_open'] = (tokens, idx, options, env, self) => {
      const token = tokens[idx];
      token.attrPush(['target', '_blank']);
      token.attrPush(['rel', 'noopener noreferrer']);
      return self.renderToken(tokens, idx, options);
    };
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      if (this.chatMessagesContainer) {
        const element = this.chatMessagesContainer.nativeElement;
        element.scrollTo({
          top: element.scrollHeight,
          behavior: 'smooth'
        });
      }
    } catch (err) {
    }
  }

  toggleChat(): void {
    this.isExpanded = !this.isExpanded;
    
    if (this.isExpanded && !this.isInitialized) {
      this.initializeChat();
    }
  }

  private initializeChat(): void {
    this.translateService.get('common.chatbot.welcomeMessage').subscribe(welcomeText => {
      const welcomeMessage: ChatMessage = {
        message: welcomeText,
        isUser: false,
        timestamp: new Date()
      };
      this.messages.push(welcomeMessage);
    });
    
    this.isInitialized = true;
    
    // Scroll to bottom after welcome message
    setTimeout(() => this.scrollToBottom(), 100);
  }

  closeChat(): void {
    this.isExpanded = false;
  }

  sendMessage(): void {
    if (!this.currentMessage.trim() || this.isLoading) {
      return;
    }

    const userMessage: ChatMessage = {
      message: this.currentMessage.trim(),
      isUser: true,
      timestamp: new Date()
    };
    this.messages.push(userMessage);

    setTimeout(() => this.scrollToBottom(), 50);

    const messageToSend = this.currentMessage.trim();
    this.currentMessage = '';
    this.isLoading = true;

    this.chatbotService.getChatbotResponse(messageToSend).subscribe({
      next: (response: ChatbotResponse) => {
        if (response.success && response.message) {
          const botMessage: ChatMessage = {
            message: response.message,
            isUser: false,
            timestamp: new Date()
          };
          this.messages.push(botMessage);
          
          setTimeout(() => this.scrollToBottom(), 100);
        } else {
          this.translateService.get('common.chatbot.errorMessage').subscribe(errorText => {
            const errorMessage: ChatMessage = {
              message: response.error || errorText,
              isUser: false,
              timestamp: new Date()
            };
            this.messages.push(errorMessage);
            
            setTimeout(() => this.scrollToBottom(), 100);
          });
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error getting chatbot response:', error);
        this.translateService.get('common.chatbot.connectionError').subscribe(connectionErrorText => {
          const errorMessage: ChatMessage = {
            message: connectionErrorText,
            isUser: false,
            timestamp: new Date()
          };
          this.messages.push(errorMessage);
          this.isLoading = false;
          
          setTimeout(() => this.scrollToBottom(), 100);
        });
      }
    });
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  getSanitizedMessage(message: string): SafeHtml {
    const html = this.markdownParser.render(message);
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  parseMarkdownToHtml(markdown: string): SafeHtml {
    const html = this.markdownParser.render(markdown);
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}