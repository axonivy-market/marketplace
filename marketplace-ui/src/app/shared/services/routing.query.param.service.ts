import { computed, inject, Injectable, signal } from '@angular/core';
import { DESIGNER_SESSION_STORAGE_VARIABLE } from '../constants/common.constant';
import { Router, Params, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
@Injectable({
  providedIn: 'root'
})
export class RoutingQueryParamService {
  // private readonly isDesigner = signal(false);
  // isDesignerEnv = computed(() => this.isDesigner());
  // designerVersion = signal('');

  // constructor(private readonly router: Router) {
  //   this.getNavigationStartEvent().subscribe(() => {
  //     if (!this.isDesigner()) {
  //       this.isDesigner.set(
  //         sessionStorage.getItem(
  //           DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName
  //         ) === DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
  //       );
  //     }
  //   });
  // }

  // checkSessionStorageForDesignerVersion(params: Params): void {
  //   const versionParam =
  //     params[DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName];
  //   if (versionParam !== undefined) {
  //     sessionStorage.setItem(
  //       DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName,
  //       versionParam
  //     );
  //     this.designerVersion.set(versionParam);
  //   }
  // }

  // checkSessionStorageForDesignerEnv(params: Params): void {
  //   const ivyViewerParam =
  //     params[DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName];
  //   if (
  //     ivyViewerParam === DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
  //   ) {
  //     sessionStorage.setItem(
  //       DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName,
  //       ivyViewerParam
  //     );
  //     this.isDesigner.set(true);
  //   }
  // }

  // getDesignerVersionFromSessionStorage(): string {
  //   if (this.designerVersion() === '') {
  //     this.designerVersion.set(
  //       sessionStorage.getItem(
  //         DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName
  //       ) ?? ''
  //     );
  //   }
  //   return this.designerVersion();
  // }

  // isDesignerViewer(): boolean {
  //   if (!this.isDesigner()) {
  //     this.isDesigner.set(
  //       sessionStorage.getItem(
  //         DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName
  //       ) === DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
  //     );
  //   }
  //   return this.isDesigner();
  // }

  // getNavigationStartEvent() {
  //   return this.router.events.pipe(
  //     filter(event => event instanceof NavigationStart)
  //   );
  // }
    private readonly isDesigner = signal(false);
  isDesignerEnv = computed(() => this.isDesigner());
  designerVersion = signal('');

  private readonly storageRef = inject(SessionStorageRef); // ✅ inject storageRef

  constructor(private readonly router: Router) {
    this.getNavigationStartEvent().subscribe(() => {
      if (!this.isDesigner()) {
        const value = this.storageRef.session?.getItem(
          DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName
        );
        this.isDesigner.set(
          value === DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
        );
      }
    });
  }

  checkSessionStorageForDesignerVersion(params: Params): void {
    const versionParam =
      params[DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName];
    if (versionParam !== undefined) {
      this.storageRef.session?.setItem(
        DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName,
        versionParam
      );
      this.designerVersion.set(versionParam);
    }
  }

  checkSessionStorageForDesignerEnv(params: Params): void {
    const ivyViewerParam =
      params[DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName];
    if (
      ivyViewerParam === DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
    ) {
      this.storageRef.session?.setItem(
        DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName,
        ivyViewerParam
      );
      this.isDesigner.set(true);
    }
  }

  getDesignerVersionFromSessionStorage(): string {
    if (this.designerVersion() === '') {
      const stored = this.storageRef.session?.getItem(
        DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName
      );
      this.designerVersion.set(stored ?? '');
    }
    return this.designerVersion();
  }

  isDesignerViewer(): boolean {
    if (!this.isDesigner()) {
      const viewer = this.storageRef.session?.getItem(
        DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName
      );
      this.isDesigner.set(
        viewer === DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
      );
    }
    return this.isDesigner();
  }

  getNavigationStartEvent() {
    return this.router.events.pipe(
      filter(event => event instanceof NavigationStart)
    );
  }
}
