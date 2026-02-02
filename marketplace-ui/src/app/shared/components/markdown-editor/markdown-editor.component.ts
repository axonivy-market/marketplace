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
  isMDEReady = false;

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
    this.isMDEReady = true;

    this.mde.codemirror.on('change', () => {
      // emit changes if needed
      this.cdr.markForCheck();
    });

    queueMicrotask(() => this.cdr.detectChanges());
  }

  ngOnDestroy(): void {
    this.mde?.toTextArea();
    this.mde = undefined;
  }
}
