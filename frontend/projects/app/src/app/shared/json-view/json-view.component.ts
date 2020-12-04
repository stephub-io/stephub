import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

@Component({
  selector: "sh-json-view",
  templateUrl: "./json-view.component.html",
  styleUrls: ["./json-view.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JsonViewComponent {
  @Input() json: any;

  highlightString(): string {
    return JSON.stringify(this.json, null, 2);
  }
}
