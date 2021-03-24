import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import {
  ROUTE_ANIMATIONS_ELEMENTS,
  routeAnimations,
} from "../../../core/core.module";
import { WorkspaceService } from "./workspace.service";
import { Workspace } from "./workspace.model";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { ActivatedRoute } from "@angular/router";
import { Title } from "@angular/platform-browser";
import { environment as env } from "../../../../environments/environment";
import { BreakpointObserver, Breakpoints } from "@angular/cdk/layout";
import { BreadcrumbService } from "xng-breadcrumb";

@Component({
  selector: "sh-workspace",
  templateUrl: "./workspace.component.html",
  styleUrls: ["./workspace.component.scss"],
  animations: [routeAnimations],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  id: string;
  workspace$: Observable<Workspace>;

  links = [];
  isHandset$: Observable<boolean> = this.breakpointObserver
    .observe(Breakpoints.Handset)
    .pipe(map((result) => result.matches));

  constructor(
    private workspaceService: WorkspaceService,
    private route: ActivatedRoute,
    private titleService: Title,
    private breakpointObserver: BreakpointObserver,
    private breadcrumbService: BreadcrumbService
  ) {
    this.route.params.subscribe((params) => (this.id = params.wid));
  }

  ngOnInit() {
    this.workspace$ = this.workspaceService.get(this.id).pipe(
      map((workspace) => {
        this.titleService.setTitle(`${workspace.name} - ${env.appName}`);
        this.links = [
          {
            link: ["/workspaces", workspace.id],
            label: "Details",
            exact: true,
          },
          {
            link: ["/workspaces", workspace.id, "executions"],
            label: "Executions",
          },
          {
            link: ["/workspaces", workspace.id, "loadExecutions"],
            label: "Load tests",
          },
        ];
        this.breadcrumbService.set("@workspace", workspace.name);
        return workspace;
      })
    );
  }
}
