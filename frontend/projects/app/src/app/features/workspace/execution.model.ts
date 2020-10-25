import { StepResponse } from "./step.model";
import { GherkinPreferences } from "./workspace/workspace.model";
import { StepSpec } from "./step/step.model";

export interface Execution {
  id: string;
  startedAt: string;
  initiatedAt: string;
  completedAt: string;
  erroneous: boolean;
  status: ExecutionStatus;
  stats: Stats;
  backlog: ExecutionItem[];
  gherkinPreferences: GherkinPreferences;
}

export interface ExecutionItem {
  type: string;
  erroneous: boolean;
  status: ExecutionStatus;
  stats: Stats;
}

export interface FeatureExecutionItem extends ExecutionItem {
  name: string;
  scenarios: ScenarioExecutionItem[];
}

export interface ScenarioExecutionItem extends ExecutionItem {
  name: string;
  steps: StepExecutionItem[];
}

export interface StepExecutionItem extends ExecutionItem {
  id: string;
  step: string;
  stepSpec: StepSpec;
  response: StepResponse;
}

export enum ExecutionStatus {
  initiated = "initiated",
  executing = "executing",
  completed = "completed",
  cancelled = "cancelled",
}

export interface Stats {
  passed: number;
  failed: number;
  erroneous: number;
}

export interface ExecutionsResult {
  total: number;
  items: Execution[];
}
