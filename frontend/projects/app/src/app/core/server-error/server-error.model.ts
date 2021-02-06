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
    propagateFieldErrors(this.errors, fieldPath, formControl);
  }
}

export function propagateFieldErrors(
  fieldErrors: FieldError[],
  fieldPath: string,
  formControl: FormControl
) {
  if (fieldErrors) {
    const fes = fieldErrors.filter((value) => value.field == fieldPath);
    if (fes.length > 0) {
      formControl.updateValueAndValidity({
        emitEvent: true,
      });
      formControl.setErrors({
        server: fes.map((value) => value.message).join(" - "),
      });
      formControl.markAllAsTouched();
    }
  }
}

export class FieldError {
  field: string;
  message: string;
  rejectedValue: any;
}
