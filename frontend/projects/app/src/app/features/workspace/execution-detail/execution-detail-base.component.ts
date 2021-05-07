import { Component, OnDestroy, OnInit } from "@angular/core";
import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../core/animations/route.animations";
import { BehaviorSubject } from "rxjs";
import { Execution, ExecutionStatus, FixtureType } from "../execution.model";
import { ExecutionService } from "../execution.service";
import { ActivatedRoute } from "@angular/router";
import { BreadcrumbService } from "xng-breadcrumb";
import { DatePipe } from "@angular/common";
import { map } from "rxjs/operators";
import {
  faCheckCircle,
  faHandPaper,
  faPauseCircle,
  faTimesCircle,
} from "@fortawesome/free-regular-svg-icons";
import { faSpinner } from "@fortawesome/free-solid-svg-icons";
import { NotificationService } from "../../../core/notifications/notification.service";

@Component({
  template: "",
})
export abstract class ExecutionDetailBaseComponent<T extends Execution>
  implements OnInit, OnDestroy {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  wid: string;
  id: string;
  execution$ = new BehaviorSubject<T>(null);
  after = FixtureType.after;
  before = FixtureType.before;
  executionLoadedAt: Date;
  private stopRefreshing = false;
  serverAction: boolean;

  protected constructor(
    protected executionService: ExecutionService,
    protected route: ActivatedRoute,
    protected breadcrumbService: BreadcrumbService,
    protected datePipe: DatePipe,
    protected notificationService: NotificationService
  ) {
    this.route.parent.parent.params.subscribe((params) => {
      this.wid = params.wid;
    });
    this.route.params.subscribe((params) => {
      this.id = params.id;
    });
  }

  ngOnInit() {
    this.executionService
      .get<T>(this.wid, this.id)
      .pipe(
        map((execution) => {
          this.injectExecution(execution);
          return execution;
        })
      )
      .subscribe();
  }

  stop() {
    this.serverAction = true;
    return this.executionService
      .stop<T>(this.wid, this.id)
      .pipe(
        map((execution) => {
          this.injectExecution(execution);
          this.notificationService.success("Execution is getting stopped");
          return execution;
        })
      )
      .subscribe(
        () => (this.serverAction = false),
        (e) => {
          this.serverAction = false;
          throw e;
        }
      );
  }

  statusIcon(status: ExecutionStatus) {
    return statusIcon(status);
  }

  statusIconSpin(status: ExecutionStatus) {
    return statusIconSpin(status);
  }

  ngOnDestroy(): void {
    this.stopRefreshing = true;
  }

  private injectExecution(execution: T) {
    this.breadcrumbService.set(
      "@execution",
      execution.startedAt
        ? this.datePipe.transform(execution.startedAt, "medium")
        : execution.status == ExecutionStatus.initiated
        ? "Starting soon"
        : execution.id
    );
    this.executionLoadedAt = new Date();
    this.execution$.next(execution);
  }
}

export function statusIcon(status: ExecutionStatus) {
  switch (status) {
    case ExecutionStatus.cancelled:
      return faTimesCircle;
    case ExecutionStatus.completed:
      return faCheckCircle;
    case ExecutionStatus.initiated:
      return faPauseCircle;
    case ExecutionStatus.executing:
      return faSpinner;
    case ExecutionStatus.stopping:
      return faHandPaper;
  }
}

export function statusIconSpin(status: ExecutionStatus) {
  return status == ExecutionStatus.executing;
}
