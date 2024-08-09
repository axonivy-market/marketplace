import {Component, inject, Input} from '@angular/core';
import {TranslateModule} from "@ngx-translate/core";
import { LanguageService } from '../../../../core/services/language/language.service';

@Component({
  selector: 'app-product-installation-count-action',
  standalone: true,
    imports: [
        TranslateModule
    ],
  templateUrl: './product-installation-count-action.component.html',
  styleUrl: './product-installation-count-action.component.scss'
})

export class ProductInstallationCountActionComponent {
  @Input()
  currentInstallationCount!: number;

  languageService = inject(LanguageService);
}
