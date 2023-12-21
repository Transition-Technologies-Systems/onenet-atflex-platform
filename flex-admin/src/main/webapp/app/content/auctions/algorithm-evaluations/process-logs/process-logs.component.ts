import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Subject, takeUntil } from 'rxjs';

import { AlgorithmEvaluationLogDTO } from '../algorithm-evaluation';
import { AlgorithmEvaluationsService } from '../algorithm-evaluations.service';
import { RxStompService } from '@stomp/ng2-stompjs';

@Component({
  selector: 'app-auctions-algorithm-process-logs',
  templateUrl: './process-logs.component.html',
  styleUrls: ['./process-logs.component.scss'],
})
export class AlgorithmEvaluationsProcessLogsComponent implements OnInit, OnDestroy {
  data: AlgorithmEvaluationLogDTO[] = [];

  get evaluationId(): number {
    return this.config.data.id;
  }

  private destroy$ = new Subject<void>();

  constructor(
    public ref: DynamicDialogRef,
    private cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig,
    private rxStompService: RxStompService,
    private service: AlgorithmEvaluationsService
  ) {}

  ngOnInit(): void {
    this.getLogs();
    this.watchRxStomp();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  close(): void {
    this.ref.close();
  }

  private getLogs(): void {
    this.service.getLogs(this.evaluationId).subscribe((response: AlgorithmEvaluationLogDTO[]) => {
      this.data = response;
    });
  }

  private watchRxStomp(): void {
    this.rxStompService
      .watch(`/refresh-view/auctions/algorithm-evaluations/logs/${this.evaluationId}`)
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        const data: AlgorithmEvaluationLogDTO = JSON.parse(message.body || '');

        if (!!data) {
          const dataIndex = this.data.findIndex(({ fileName }) => fileName === data.fileName);

          if (dataIndex !== -1) {
            this.data[dataIndex] = data;

            this.data = [...this.data];
          } else {
            this.data = [...this.data, data];
          }

          this.cdr.markForCheck();
        }
      });
  }
}
