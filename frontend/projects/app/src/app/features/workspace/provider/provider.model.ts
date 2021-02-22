import { StepSpec } from "../step/step.model";

export interface ProviderInfo {
  name: string;
  version: string;
  optionsSchema?: object;
  steps: StepSpec[];
}
