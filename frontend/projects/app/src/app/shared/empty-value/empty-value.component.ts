import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

@Component({
  selector: "sh-empty-value",
  templateUrl: "./empty-value.component.html",
  styleUrls: ["./empty-value.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyValueComponent {
  @Input() value: any;
}
