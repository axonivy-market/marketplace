import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { CommonDropdownComponent } from "../../shared/components/common-dropdown/common-dropdown.component";
import { TranslateModule } from "@ngx-translate/core";
import { MultilingualismPipe } from "../../shared/pipes/multilingualism.pipe";
import { Component, inject, ViewEncapsulation } from "@angular/core";
import { AuthService } from "../../auth/auth.service";
import { AppModalService } from "../../shared/services/app-modal.service";

@Component({
  selector: 'app-comment-approval',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CommonDropdownComponent,
    TranslateModule,
    MultilingualismPipe
  ],
  templateUrl: './comment-approval.component.html',
  styleUrls: ['./comment-approval.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class CommentApprovalComponent {
  isAuthenticated = false;

  authService = inject(AuthService);
  appModalService = inject(AppModalService);

  ngOnInit(): void {
    if (!this.authService.getToken()) {
      this.authService.redirectToGitHub('comment-approval');
    }
  }
}
