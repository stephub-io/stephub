import { ChangeDetectionStrategy, Component, Inject } from "@angular/core";
import {
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from "@angular/forms";
import { Variable } from "../workspace/workspace.model";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import "brace";
import "brace/mode/json";
import "brace/theme/github";
import {
  getSchemaType,
  JsonSchemaType,
} from "../../../shared/json-schema-view/json--schema-view.component";
import { ServerError } from "../../../core/server-error/server-error.model";
import { Observable } from "rxjs";

@Component({
  selector: "sh-variable-dialog",
  templateUrl: "./variable-dialog.component.html",
  styleUrls: ["./variable-dialog.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VariableDialogComponent {
  form: FormGroup;

  variable: Variable;
  nameField = new FormControl("", [
    Validators.required,
    Validators.pattern(/^[a-zA-Z_][a-zA-Z0-9_]*$/),
  ]);

  valueField = new FormControl("", []);
  valueJsonStr: string;
  valueNull: boolean;

  schema: object;
  schemaString: string;
  schemaType: JsonSchemaType;
  schemaTypes = [
    JsonSchemaType.string,
    JsonSchemaType.number,
    JsonSchemaType.boolean,
    JsonSchemaType.any,
    JsonSchemaType.custom,
  ];
  schemaField = new FormControl("", []);

  private readonly saveCallback: (data) => Observable<any>;
  private readonly name: string;
  readonly mode = VariableDialogMode.full;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<VariableDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data
  ) {
    this.variable = data.variable;
    this.name = data.name;
    this.saveCallback = data.saveCallback;
    this.mode = data.mode ? data.mode : VariableDialogMode.full;
    this.form = fb.group([this.nameField, this.valueField, this.schemaField]);
    this.nameField.setValue(this.name);
    this.schemaType = getSchemaType(this.variable.schema);
    this.schemaString = JSON.stringify(this.variable.schema, null, 2);
    const value =
      this.mode == VariableDialogMode.full
        ? this.variable.defaultValue
        : data.value;
    this.applyFromValue(value);
    this.valueNull = value == null;
    this.changeValueNull();
    this.updateValueValidator();
  }

  buildSchema() {
    switch (this.schemaType) {
      case JsonSchemaType.string:
        return { type: "string" };
      case JsonSchemaType.number:
        return { type: "number" };
      case JsonSchemaType.boolean:
        return { type: "boolean" };
      case JsonSchemaType.any:
        return null;
      case JsonSchemaType.custom:
        try {
          return JSON.parse(this.schemaString);
        } catch {
          return null;
        }
    }
  }

  get aceValue() {
    return this.valueJsonStr;
  }

  set aceValue(value) {
    this.valueJsonStr = value;
    try {
      this.valueField.updateValueAndValidity({
        emitEvent: true,
      });
      this.valueField.setValue(JSON.parse(value));
    } catch (e) {
      this.valueField.setErrors({
        json: e.message,
      });
    }
  }

  changeSchema() {
    this.schemaString = JSON.stringify(this.buildSchema(), null, 4);
  }

  applyFromValue(value) {
    switch (this.schemaType) {
      case JsonSchemaType.boolean:
        this.valueField.setValue(!!value);
        break;
      case JsonSchemaType.string:
        this.valueField.setValue(value != null ? value + "" : "");
        break;
      case JsonSchemaType.number:
        this.valueField.setValue(Number(value));
        break;
      case JsonSchemaType.any:
      case JsonSchemaType.custom:
        this.valueJsonStr = JSON.stringify(value, null, 4);
        this.valueField.setValue(value);
    }
  }

  save() {
    let data;
    const value = this.valueNull ? null : this.valueField.value;
    if ((this.mode as VariableDialogMode) == VariableDialogMode.value) {
      this.dialogRef.close(
        (data = {
          value: value,
        })
      );
      return;
    } else {
      data = {
        name: this.nameField.value,
        variable: {
          schema: this.buildSchema(),
          defaultValue: value,
        } as Variable,
      };
    }
    const subject = this.saveCallback(data);
    if (subject) {
      subject.subscribe(
        () => {
          this.close();
        },
        (error) => {
          if (error instanceof ServerError && error.status == 400) {
            console.log("Bad request", error);
            const prefix = "variables[" + this.nameField.value + "]";
            error.propagateFieldErrors(prefix, this.nameField);
            error.propagateFieldErrors(
              prefix + ".defaultValue",
              this.valueField
            );
            error.propagateFieldErrors(prefix + ".schema", this.schemaField);
            this.form.markAllAsTouched();
          } else {
            throw error;
          }
        }
      );
    } else {
      this.close();
    }
  }

  close() {
    this.dialogRef.close();
  }

  changeType() {
    this.applyFromValue(this.valueField.value);
    this.changeSchema();
    this.updateValueValidator();
  }

  private updateValueValidator() {
    switch (this.schemaType) {
      case JsonSchemaType.any:
      case JsonSchemaType.custom:
      case JsonSchemaType.string:
        this.valueField.setValidators([]);
        break;
      default:
        this.valueField.setValidators([Validators.required]);
    }
  }

  changeValueNull() {
    if (this.valueNull) {
      this.valueField.disable();
    } else {
      this.valueField.enable();
    }
  }
}

export enum VariableDialogMode {
  full = "full",
  value = "value",
}
