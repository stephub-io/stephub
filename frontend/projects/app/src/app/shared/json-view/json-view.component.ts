import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

@Component({
  selector: "sh-json-view",
  templateUrl: "./json-view.component.html",
  styleUrls: ["./json-view.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JsonViewComponent {
  @Input() json: any;

  @Input() jsonStr: any;

  highlightString(): string {
    return this.jsonStr ? this.jsonStr : JSON.stringify(this.json, null, 2);
  }
}
