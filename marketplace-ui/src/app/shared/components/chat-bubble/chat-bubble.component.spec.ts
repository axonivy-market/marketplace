import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { ChatBubbleComponent } from './chat-bubble.component';
import { ChatbotServiceService } from '../../../modules/chatbot/chatbot-service.service';

describe('ChatBubbleComponent', () => {
  let component: ChatBubbleComponent;
  let fixture: ComponentFixture<ChatBubbleComponent>;
  let mockChatbotService: jasmine.SpyObj<ChatbotServiceService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('ChatbotServiceService', ['getChatbotResponse']);

    await TestBed.configureTestingModule({
      imports: [ChatBubbleComponent, FormsModule],
      providers: [
        { provide: ChatbotServiceService, useValue: spy }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ChatBubbleComponent);
    component = fixture.componentInstance;
    mockChatbotService = TestBed.inject(ChatbotServiceService) as jasmine.SpyObj<ChatbotServiceService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});