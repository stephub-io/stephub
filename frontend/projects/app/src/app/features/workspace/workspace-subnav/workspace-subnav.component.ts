import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  SimpleChanges,
} from "@angular/core";
import { ROUTE_ANIMATIONS_ELEMENTS } from "../../../core/core.module";
import { faForward as faExecutions } from "@fortawesome/free-solid-svg-icons";
import { Workspace } from "../workspace/workspace.model";

@Component({
  selector: "sh-workspace-subnav",
  templateUrl: "./workspace-subnav.component.html",
  styleUrls: ["./workspace-subnav.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceSubnavComponent implements OnChanges {
  routeAnimationsElements = ROUTE_ANIMATIONS_ELEMENTS;

  faExecutions = faExecutions;

  @Input() workspace: Workspace;

  links = [];

  constructor() {}

  ngOnChanges(changes: SimpleChanges): void {
    if (this.workspace) {
      this.links = [
        { link: ["/workspaces", this.workspace.id], label: "Details" },
        {
          link: ["/workspaces", this.workspace.id, "executions"],
          label: "Executions",
        },
      ];
    }
  }
}
