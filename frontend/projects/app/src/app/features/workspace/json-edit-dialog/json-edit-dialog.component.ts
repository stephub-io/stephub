import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnInit,
} from "@angular/core";
import { FormBuilder, FormControl, FormGroup } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import "brace";
import "brace/mode/json";
import "brace/theme/github";
import { isNullable } from "../../../shared/json-schema-view/json--schema-view.component";

@Component({
  selector: "sh-json-form-dialog",
  templateUrl: "./json-edit-dialog.component.html",
  styleUrls: ["./json-edit-dialog.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JsonEditDialogComponent implements OnInit {
  form: FormGroup;
  schema: any;
  valueControl: FormControl;
  formSchema: any;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<JsonEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data
  ) {
    this.valueControl = new FormControl(data.value);
    this.schema = data.schema;
    if (this.schema) {
      this.formSchema = JSON.parse(JSON.stringify(this.schema));
    }
    this.form = fb.group([this.valueControl]);
  }

  ngOnInit(): void {}

  save() {
    this.dialogRef.close({
      value: this.valueControl.value,
    });
  }

  close() {
    this.dialogRef.close();
  }

  nullSchema(schema: any): boolean {
    return isNullable(schema);
  }

  get value() {
    return this.valueControl.value;
  }

  set value(value: string) {
    this.valueControl.updateValueAndValidity({
      emitEvent: true,
    });
    this.valueControl.setValue(value);
  }

  get aceValue() {
    return JSON.stringify(this.valueControl.value, null, 4);
  }

  set aceValue(value: string) {
    try {
      this.valueControl.updateValueAndValidity({
        emitEvent: true,
      });
      this.valueControl.setValue(JSON.parse(value));
    } catch (e) {
      this.valueControl.setErrors({
        json: e.message,
      });
    }
  }
}
