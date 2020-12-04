import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import "brace";
import "brace/mode/json";
import "brace/theme/github";
import { Workspace } from "../../workspace/workspace.model";
import {
  faMagic,
  faReceipt,
  faRocket,
} from "@fortawesome/free-solid-svg-icons";

@Component({
  selector: "sh-workspace-features",
  templateUrl: "./workspace-features.component.html",
  styleUrls: ["./workspace-features.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceFeaturesComponent {
  featureIcon = faRocket;
  scenarioIcon = faReceipt;
  stepIcon = faMagic;

  @Input() workspace: Workspace;

  constructor() {}

  ngOnInit() {}
}
