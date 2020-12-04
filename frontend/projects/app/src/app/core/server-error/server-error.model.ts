import { FormControl } from "@angular/forms";

export class ServerError implements Error {
  error: string;
  errors?: FieldError[];
  message: string;
  path: string;
  status: number;
  timestamp: string;
  name = "ServerError";

  propagateFieldErrors(fieldPath: string, formControl: FormControl) {
    if (this.errors) {
      const fes = this.errors.filter((value) => value.field == fieldPath);
      if (fes.length > 0) {
        formControl.updateValueAndValidity({
          emitEvent: true,
        });
        formControl.setErrors({
          server: fes.map((value) => value.message).join(" - "),
        });
        formControl.markAsTouched();
      }
    }
  }
}

export class FieldError {
  field: string;
  message: string;
  rejectedValue: any;
}
