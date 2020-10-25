import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import {
  ROUTE_ANIMATIONS_ELEMENTS,
  routeAnimations,
} from "../../../core/core.module";
import { WorkspaceService } from "./workspace.service";
import { Workspace } from "./workspace.model";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { faForward as faExecutions } from "@fortawesome/free-solid-svg-icons";
import { ActivatedRoute } from "@angular/router";
import { Title } from "@angular/platform-browser";
import { environment as env } from "../../../../environments/environment";

@Component({
  selector: "sh-workspace-detail",
  templateUrl: "./workspace.component.html",
  styleUrls: ["./workspace.component.scss"],
  animations: [routeAnimations],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceComponent implements OnInit {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  id: string;
  workspace$: Observable<Workspace>;

  faExecutions = faExecutions;

  constructor(
    private workspaceService: WorkspaceService,
    private route: ActivatedRoute,
    private titleService: Title
  ) {
    this.route.params.subscribe((params) => (this.id = params.wid));
  }

  ngOnInit() {
    this.workspace$ = this.workspaceService.get(this.id).pipe(
      map((workspace) => {
        this.titleService.setTitle(`${workspace.name} - ${env.appName}`);
        return workspace;
      })
    );
  }
}
