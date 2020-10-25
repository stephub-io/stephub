import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

@Component({
  selector: "sh-empty-state",
  templateUrl: "./empty-state.component.html",
  styleUrls: ["./empty-state.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyStateComponent {
  @Input() title: String;

  @Input() subTitle: String;
}
