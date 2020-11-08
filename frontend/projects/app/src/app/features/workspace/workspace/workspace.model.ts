export interface Workspace {
  id: string;
  name: string;
  gherkinPreferences: GherkinPreferences;
  variables: VariableMap;
}

export interface WorkspacesResult {
  total: number;
  items: Workspace[];
}

export interface VariableMap {
  [key: string]: Variable;
}

export interface Variable {
  value: any;
  defaultValue: any;
  description: string;
  schema: object;
}

export interface GherkinPreferences {
  assignmentKeywords: string[];
  stepKeywords: string[];
}
