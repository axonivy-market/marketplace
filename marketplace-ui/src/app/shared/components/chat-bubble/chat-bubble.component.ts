import { Component, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotServiceService } from '../../../modules/chatbot/chatbot-service.service';

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
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-bubble.component.html',
  styleUrls: ['./chat-bubble.component.scss']
})
export class ChatBubbleComponent implements AfterViewChecked {
  @ViewChild('chatMessages') private chatMessagesContainer!: ElementRef;

  isExpanded = false;
  currentMessage = '';
  messages: ChatMessage[] = [];
  isLoading = false;
  private isInitialized = false;

  constructor(private chatbotService: ChatbotServiceService) {}

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
      // Handle any scrolling errors silently
    }
  }

  toggleChat(): void {
    this.isExpanded = !this.isExpanded;
    
    if (this.isExpanded && !this.isInitialized) {
      this.initializeChat();
    }
  }

  private initializeChat(): void {
    const welcomeMessage: ChatMessage = {
      message: 'Hello! Welcome to AxonIvy Marketplace chat service. How can I assist you today?',
      isUser: false,
      timestamp: new Date()
    };
    this.messages.push(welcomeMessage);
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

    // Scroll to bottom after user message
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
          
          // Scroll to bottom after bot response
          setTimeout(() => this.scrollToBottom(), 100);
        } else {
          const errorMessage: ChatMessage = {
            message: response.error || 'Sorry, something went wrong. Please try again later.',
            isUser: false,
            timestamp: new Date()
          };
          this.messages.push(errorMessage);
          
          // Scroll to bottom after error message
          setTimeout(() => this.scrollToBottom(), 100);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error getting chatbot response:', error);
        const errorMessage: ChatMessage = {
          message: 'Sorry, unable to connect to chat service. Please try again later.',
          isUser: false,
          timestamp: new Date()
        };
        this.messages.push(errorMessage);
        this.isLoading = false;
        
        // Scroll to bottom after error message
        setTimeout(() => this.scrollToBottom(), 100);
      }
    });
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}