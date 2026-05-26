import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  AfterViewInit,
  Component,
  effect,
  ElementRef,
  Inject,
  input,
  Input,
  model,
  OnDestroy,
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
export class MarkdownEditorComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mde', { static: true })
  textarea!: ElementRef<HTMLTextAreaElement>;

  @Input()
  autosaveId!: string;

  @Input()
  placeholder = 'Enter content here';

  private mde?: EasyMDE;
  isMDEReady = false;
  contentValue = model<string>('');
  isSubmittingSignal = input<Signal<boolean>>();
  textPrimaryClass = 'text-primary';
  bgSecondaryClass = 'bg-secondary';

  protected loadEasyMDE(): Promise<any> {
    return import('easymde');
  }

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {
    effect(() => {
      const value = this.contentValue() ?? '';
      const submitting = this.isSubmittingSignal()?.();

      if (!this.mde) {
        return;
      }

      this.mde.codemirror.setOption(
        'readOnly',
        submitting ? 'nocursor' : false
      );

      if (this.mde.value() !== value) {
        const cm = this.mde.codemirror;
        const cursor = cm.getCursor();
        this.mde.value(value);
        cm.setCursor(cursor);
      }
    });
  }

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.initializeEditor();
  }

  async initializeEditor(): Promise<void> {
    const { default: EasyMDE } = await this.loadEasyMDE();

    this.mde = new EasyMDE({
      element: this.textarea.nativeElement,
      autofocus: true,
      autosave: {
        enabled: false,
        uniqueId: this.autosaveId,
        delay: 500,
        submit_delay: 5000
      },
      toolbar: [
        'bold',
        'italic',
        'heading',
        'strikethrough',
        '|',
        'quote',
        'unordered-list',
        'ordered-list',
        '|',
        'code',
        '|',
        'link',
        'image',
        '|',
        'preview',
        'side-by-side',
        'fullscreen',
        '|',
        'guide'
      ],
      spellChecker: false,
      status: false,
      placeholder: this.placeholder,
      initialValue: this.contentValue(),
      previewClass: this.bgSecondaryClass
    });
    this.isMDEReady = true;

    const container = this.mde?.codemirror
      .getWrapperElement()
      .closest('.EasyMDEContainer')!;

    const easyMDEToolbar = container.querySelector('.editor-toolbar')!;
    easyMDEToolbar.classList.add(this.bgSecondaryClass, this.textPrimaryClass);

    const codeMirrorTextArea = container.querySelector('.CodeMirror')!;
    codeMirrorTextArea.classList.add(
      this.bgSecondaryClass,
      this.textPrimaryClass
    );

    this.mde?.codemirror.on('change', () => {
      this.updateContent(this.mde!.value());
    });
  }

  setEasyMDEContent(content: string): void {
    if (!this.mde) {
      return;
    }
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
