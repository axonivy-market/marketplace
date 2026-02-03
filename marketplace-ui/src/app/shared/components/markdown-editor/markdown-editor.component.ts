import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  model,
  Output,
  PLATFORM_ID,
  ViewChild
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-markdown-editor',
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  templateUrl: './markdown-editor.component.html',
  styleUrl: './markdown-editor.component.scss'
})
export class MarkdownEditorComponent {
  @ViewChild('mde', { static: true })
  textarea!: ElementRef<HTMLTextAreaElement>;

  @Input()
  autosaveId!: string;

  isMDEReady = false;
  contentValue = model<string>('');
  placeholder: string = 'Type your release letter content here...';

  private mde?: EasyMDE;

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {}

  async ngAfterViewInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;

    const { default: EasyMDE } = await import('easymde');

    this.mde = new EasyMDE({
      element: this.textarea.nativeElement,
        // toolbar: ["bold", "italic", "heading", "|", "quote", "code"],
      autofocus: true,
      autosave: {
        enabled: true,
        uniqueId: this.autosaveId,
        delay: 1000,
        submit_delay: 5000
      },
      spellChecker: false,
      status: false,
      placeholder: this.placeholder,
      initialValue: this.contentValue()
    });
    this.isMDEReady = true;

    this.mde.codemirror.on('change', () => {
      this.updateContent(this.mde!.value());
    });
  }

  updateContent(content: string): void {
    this.contentValue.set(content);
  }

  ngOnDestroy(): void {
    this.mde?.toTextArea();
    this.mde?.cleanup();
  }
}
