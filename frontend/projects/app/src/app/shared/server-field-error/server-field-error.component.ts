import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
} from "@angular/core";
import { FieldError } from "../../core/server-error/server-error.model";

@Component({
  selector: "sh-server-field-error",
  templateUrl: "./server-field-error.component.html",
  styleUrls: ["./server-field-error.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServerFieldErrorComponent implements OnChanges {
  @Input() fieldErrors: FieldError[] | null;

  @Input() displayType: DisplayType = DisplayType.full;

  @Input() path: string | string[];

  errors: string[];

  ngOnChanges(): void {
    this.errors = [];
    if (this.fieldErrors) {
      this.fieldErrors.forEach((error) => {
        if (Array.isArray(this.path)) {
          if (this.path.find((p) => error.field.startsWith(p))) {
            this.errors.push(error.message);
          }
        } else {
          if (error.field.startsWith(this.path)) {
            this.errors.push(error.message);
          }
        }
      });
    }
  }
}

export enum DisplayType {
  indicator = "indicator",
  full = "full",
}
