import { ChangeDetectionStrategy, Component } from "@angular/core";
import { DatePipe } from "@angular/common";
import { ExecutionDetailBaseComponent } from "../execution-detail-base.component";
import { LoadExecution } from "../../execution.model";
import { ExecutionService } from "../../execution.service";
import { ActivatedRoute } from "@angular/router";
import { BreadcrumbService } from "xng-breadcrumb";

@Component({
  selector: "sh-execution-detail-load",
  templateUrl: "./execution-detail-load.component.html",
  styleUrls: ["./execution-detail-load.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [DatePipe],
})
export class ExecutionDetailLoadComponent extends ExecutionDetailBaseComponent<
  LoadExecution
> {
  constructor(
    protected executionService: ExecutionService,
    protected route: ActivatedRoute,
    protected breadcrumbService: BreadcrumbService,
    protected datePipe: DatePipe
  ) {
    super(executionService, route, breadcrumbService, datePipe);
  }
}
