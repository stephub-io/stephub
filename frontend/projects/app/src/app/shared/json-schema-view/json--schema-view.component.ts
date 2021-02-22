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
  } else if (matchesSchemaVariant(schema, "number")) {
    return JsonSchemaType.number;
  } else if (matchesSchemaVariant(schema, "string")) {
    return JsonSchemaType.string;
  } else if (matchesSchemaVariant(schema, "boolean")) {
    return JsonSchemaType.boolean;
  }
  return JsonSchemaType.custom;
}

export function getSchemaExample(schema) {
  switch (getSchemaType(schema)) {
    case JsonSchemaType.any:
      return undefined;
    case JsonSchemaType.custom:
      if (matchesSchemaVariant(schema, "object")) {
        return {};
      } else if (matchesSchemaVariant(schema, "array")) {
        return [];
      }
      return undefined;
    case JsonSchemaType.boolean:
      return true;
    case JsonSchemaType.string:
      return "lorem ipsum";
    case JsonSchemaType.number:
      return 75;
  }
  return undefined;
}

export function isNullable(schema: any, only: boolean = false) {
  return (
    schema &&
    schema.type &&
    (schema.type == "null" ||
      (Array.isArray(schema.type) &&
        schema.type.indexOf("null") >= 0 &&
        (only ? schema.type.length == 1 : true)))
  );
}

function matchesSchemaVariant(schema, type) {
  return (
    deepEqual(schema, { type: type }) ||
    deepEqual(schema, { type: [type] }) ||
    deepEqual(schema, { type: [type, "null"] }) ||
    deepEqual(schema, { type: ["null", type] })
  );
}
