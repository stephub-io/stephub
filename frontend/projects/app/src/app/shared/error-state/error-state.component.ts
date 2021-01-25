import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

@Component({
  selector: "sh-error-state",
  templateUrl: "./error-state.component.html",
  styleUrls: ["./error-state.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ErrorStateComponent {
  @Input() text: String;
}
