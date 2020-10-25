export interface Workspace {
  id: string;
  name: string;
  gherkinPreferences: GherkinPreferences;
}

export interface WorkspacesResult {
  total: number;
  items: Workspace[];
}

export interface GherkinPreferences {
  assignmentKeywords: string[];
  stepKeywords: string[];
}
