import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from "@angular/core";
import * as deepEqual from "fast-deep-equal";

@Component({
  selector: "sh-json-schema-view",
  templateUrl: "./json-schema-view.component.html",
  styleUrls: ["./json-schema-view.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JsonSchemaViewComponent implements OnInit {
  @Input() schema: any;

  schemaType: JsonSchemaType;

  ngOnInit(): void {
    this.schemaType = getSchemaType(this.schema);
  }

  schemaJsonString(): string {
    return JSON.stringify(this.schema, null, 2);
  }
}

export enum JsonSchemaType {
  number = "number",
  string = "string",
  boolean = "boolean",
  any = "any",
  custom = "custom",
}

export function getSchemaType(schema) {
  if (schema == null || deepEqual(schema, {})) {
    return JsonSchemaType.any;
  } else if (deepEqual(schema, { type: "number" })) {
    return JsonSchemaType.number;
  } else if (deepEqual(schema, { type: "string" })) {
    return JsonSchemaType.string;
  } else if (deepEqual(schema, { type: "boolean" })) {
    return JsonSchemaType.boolean;
  }
  return JsonSchemaType.custom;
}
