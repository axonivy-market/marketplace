import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  effect,
  ElementRef,
  Inject,
  input,
  Input,
  model,
  PLATFORM_ID,
  Signal,
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

  isSubmittingSignal = input<Signal<boolean>>();

  placeholder: string = 'Type your release letter content here...';

  private mde?: EasyMDE;

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    effect(() => {
      const value = this.contentValue() ?? '';
      const submitting = this.isSubmittingSignal()?.();

      if (!this.mde) return; 
      this.mde.codemirror.setOption('readOnly', submitting ? 'nocursor' : false);

      if (this.mde.value() !== value) {
        const cm = this.mde.codemirror;
        const cursor = cm.getCursor();
        this.mde.value(value);
        cm.setCursor(cursor);
      }
    });
  }

  async ngAfterViewInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;

    const { default: EasyMDE } = await import('easymde');

    this.mde = new EasyMDE({
      element: this.textarea.nativeElement,
      autofocus: true,
      autosave: {
        enabled: false,
        uniqueId: this.autosaveId,
        delay: 500,
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

  setEasyMDEContent(content: string): void {
    if (!this.mde) return;
    this.mde.value(content);
  }

  updateContent(content: string): void {
    this.contentValue.set(content);
  }

  ngOnDestroy(): void {
    this.mde?.toTextArea();
    this.mde?.cleanup();
  }
}
