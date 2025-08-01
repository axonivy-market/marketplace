import { computed, Injectable, signal } from '@angular/core';
import { DESIGNER_SESSION_STORAGE_VARIABLE } from '../constants/common.constant';
import { Router, Params, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
@Injectable({
  providedIn: 'root'
})
export class RoutingQueryParamService {
  private readonly isDesigner = signal(false);
  isDesignerEnv = computed(() => this.isDesigner());
  designerVersion = signal('');

  constructor(private readonly router: Router, private readonly storageRef: SessionStorageRef) {
    this.getNavigationStartEvent().subscribe(() => {
      if (!this.isDesigner()) {
        const savedIvyViewer = this.storageRef.session?.getItem(
          DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName
        );
        this.isDesigner.set(
          savedIvyViewer === DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
        );
      }
    });
  }

  checkSessionStorageForDesignerVersion(params: Params): void {
    const version =
      params[DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName] ??
      params[DESIGNER_SESSION_STORAGE_VARIABLE.newIvyVersionParamName];

    if (version !== undefined) {
      this.storageRef.session?.setItem(
        DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName,
        version
      );
      this.designerVersion.set(version);
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
      const savedIvyVersion = this.storageRef.session?.getItem(
        DESIGNER_SESSION_STORAGE_VARIABLE.ivyVersionParamName
      );
      this.designerVersion.set(savedIvyVersion ?? '');
    }
    return this.designerVersion();
  }

  isDesignerViewer(): boolean {
    if (!this.isDesigner()) {
      const savedIvyViewer = this.storageRef.session?.getItem(
        DESIGNER_SESSION_STORAGE_VARIABLE.ivyViewerParamName
      );
      this.isDesigner.set(
        savedIvyViewer === DESIGNER_SESSION_STORAGE_VARIABLE.defaultDesignerViewer
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
