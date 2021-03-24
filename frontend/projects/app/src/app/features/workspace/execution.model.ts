import { StepStatus } from "./step.model";
import { GherkinPreferences } from "./workspace/workspace.model";
import { StepSpec } from "./step/step.model";

export enum ExecutionType {
  FUNCTIONAL = "functional",
  LOAD = "load",
}

export interface Execution {
  type: ExecutionType;
  id: string;
  startedAt: string;
  initiatedAt: string;
  completedAt: string;
  erroneous: boolean;
  status: ExecutionStatus;
}

export interface FunctionalExecution extends Execution {
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
  response: RoughStepResponse;
}

export interface RoughStepResponse {
  status: StepStatus;
  duration: string;
  errorMessage: string;
  output?: any;
  logs?: ExecutionLogEntry[];
}

export interface ExecutionLogEntry {
  message: string;
  attachments: ExecutionLogAttachment[];
}

export interface ExecutionLogAttachment {
  id: string;
  fileName: string;
  contentType: string;
  size: number;
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

export interface ExecutionsResult<E extends Execution> {
  total: number;
  items: E[];
}

export interface ExecutionStart {
  type: ExecutionType;
  sessionSettings: SessionSettings;
}

export interface FunctionalExecutionStart extends ExecutionStart {
  instruction: ExecutionInstruction;
  parallelSessionCount?: number;
  parallelizationMode: ParallelizationMode.scenario;
}

export interface ExecutionInstruction {
  type: string;
}

export interface ScenariosExecutionInstruction extends ExecutionInstruction {
  filter: ScenarioFilter;
}

export interface ScenarioFilter {
  type: string;
}

export interface SessionSettings {
  variables: VariableMap;
}

export interface VariableMap {
  [key: string]: any;
}

export enum ParallelizationMode {
  feature = "feature",
  scenario = "scenario",
}
