import { Component, OnDestroy, OnInit } from "@angular/core";
import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../core/animations/route.animations";
import { BehaviorSubject } from "rxjs";
import {
  Execution,
  ExecutionStatus,
  FixtureType,
  LoadExecution,
} from "../execution.model";
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

@Component({
  template: "",
})
export abstract class ExecutionDetailBaseComponent<T extends Execution>
  implements OnInit, OnDestroy {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  wid: string;
  id: string;
  execution$ = new BehaviorSubject<LoadExecution>(null);
  after = FixtureType.after;
  before = FixtureType.before;
  executionLoadedAt: Date;
  private stopRefreshing = false;

  protected constructor(
    protected executionService: ExecutionService,
    protected route: ActivatedRoute,
    protected breadcrumbService: BreadcrumbService,
    protected datePipe: DatePipe
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
      .get<LoadExecution>(this.wid, this.id)
      .pipe(
        map((execution) => {
          this.breadcrumbService.set(
            "@execution",
            execution.startedAt
              ? this.datePipe.transform(execution.startedAt, "medium")
              : "Starting soon"
          );
          this.executionLoadedAt = new Date();
          /*
          if (
            execution.status != ExecutionStatus.completed &&
            !this.stopRefreshing
          ) {
            setTimeout(() => this.ngOnInit(), 2000);
          }
          */
          this.execution$.next(execution);
          return execution;
        })
      )
      .subscribe();
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
