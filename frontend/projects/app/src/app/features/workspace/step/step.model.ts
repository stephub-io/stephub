export interface StepSpec {
  id: string;
  pattern: string;
  output?: OutputSpec;
  arguments: ArgumentSpec[];
  payload: PayloadType;
  dataTable?: DataTableSpec;
  docString?: DocStringSpec;
  description?: string;
}

export interface Documentation {
  description?: string;
  examples?: DocumentationExample[];
}

export interface DocumentationExample {
  description: string;
  value: string;
}

export interface ValueSpec {
  schema?: object;
  strict?: boolean;
  doc?: Documentation;
}

export interface ArgumentSpec extends ValueSpec {
  name: string;
}

export interface OutputSpec {
  schema?: object;
  doc?: Documentation;
}

export interface DataTableSpec {
  header: boolean;
  columns: ColumnSpec[];
  description?: string;
}

export interface ColumnSpec extends ValueSpec {
  name: string;
}

export interface DocStringSpec extends ValueSpec {}

export enum PayloadType {
  NONE = "none",
  DOC_STRING = "doc_string",
  DATA_TABLE = "data_table",
}
