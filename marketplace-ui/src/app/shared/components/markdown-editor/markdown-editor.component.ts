import { isPlatformBrowser } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  Output,
  PLATFORM_ID,
  ViewChild
} from '@angular/core';

@Component({
  selector: 'app-markdown-editor',
  imports: [],
  templateUrl: './markdown-editor.component.html',
  styleUrl: './markdown-editor.component.scss'
})
export class MarkdownEditorComponent {
  @ViewChild('mde', { static: true })
  textarea!: ElementRef<HTMLTextAreaElement>;
  @Input() value = '';
  @Input() placeholder = 'Write something...';
  @Input() autosaveId?: string; // optional
  @Output() valueChange = new EventEmitter<string>();

  private mde?: EasyMDE;

  constructor(@Inject(PLATFORM_ID) private readonly platformId: Object, private cdr: ChangeDetectorRef) {}

  async ngAfterViewInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;

    // Dynamic import so it only runs in the browser
    const { default: EasyMDE } = await import('easymde');

    this.mde = new EasyMDE({
      element: this.textarea.nativeElement,
      spellChecker: false,
      status: false,
    });

    this.mde.codemirror.on('change', () => {
      // emit changes if needed
      this.cdr.markForCheck();
    });

    queueMicrotask(() => this.cdr.detectChanges());
  }

  // ngAfterViewInit() {

  //   // if (isPlatformBrowser(this.platformId)) {
  //   //   this.mde = new EasyMDE({
  //   //     element: this.textarea.nativeElement,
  //   //     initialValue: this.value,
  //   //     placeholder: this.placeholder,
  //   //     spellChecker: false,
  //   //     autosave: this.autosaveId
  //   //       ? { enabled: true, uniqueId: this.autosaveId, delay: 1000 }
  //   //       : undefined,
  //   //     status: false,
  //   //     toolbar: [
  //   //       'bold',
  //   //       'italic',
  //   //       'heading',
  //   //       '|',
  //   //       'quote',
  //   //       'unordered-list',
  //   //       'ordered-list',
  //   //       '|',
  //   //       'link',
  //   //       'image',
  //   //       'table',
  //   //       '|',
  //   //       'preview',
  //   //       'side-by-side',
  //   //       'fullscreen',
  //   //       '|',
  //   //       'guide'
  //   //     ]
  //   //   });

  //   //   this.mde.codemirror.on('change', () => {
  //   //     this.valueChange.emit(this.mde!.value());
  //   //   });
  //   // }
  // }

  ngOnDestroy(): void {
    this.mde?.toTextArea();
    this.mde = undefined;
  }
}
