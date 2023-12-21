import { BehaviorSubject } from 'rxjs';

/**
 * Service to support showing loading information
 */
export class LoadingService {
  /**
   * BehaviorSubject about loading status
   */
  loading: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

  /**
   * Current request in progress
   */
  private requestInProgress = 0;

  /**
   * Current timeout instance
   */
  private timeoutCancel: any;

  /**
   * Function after change route
   */
  changeRouter() {
    this.clearTimeout();
    this.loading.next(true);
    this.timeoutCancel = setTimeout(() => this.checkComplete(), 200);
  }

  /**
   * Add pendning request
   */
  addRequest() {
    this.clearTimeout();

    if (this.requestInProgress === 0) {
      this.startLoading();
    }

    this.requestInProgress++;
  }

  /**
   * Remove request
   */
  removeRequest() {
    this.clearTimeout();
    this.requestInProgress = Math.max(0, this.requestInProgress - 1);
    this.timeoutCancel = setTimeout(() => this.checkComplete(), 200);
  }

  /**
   * Start loading
   */
  startLoading() {
    this.loading.next(true);
  }

  /**
   * Stop loading
   */
  stopLoading() {
    if (this.requestInProgress === 0) {
      this.loading.next(false);
    }
  }

  /**
   * Check if all requests are completed
   */
  private checkComplete() {
    if (this.requestInProgress === 0) {
      this.loading.next(false);
    }
  }

  /**
   * Clear timeout if it exist
   */
  private clearTimeout() {
    if (this.timeoutCancel) {
      clearTimeout(this.timeoutCancel);
    }
  }
}
