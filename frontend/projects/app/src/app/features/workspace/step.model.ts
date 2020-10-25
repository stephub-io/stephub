export interface StepResponse {
  status: StepStatus;
  duration: string;
  errorMessage: string;
}

export enum StepStatus {
  passed = "passed",
  failed = "failed",
  erroneous = "erroneous",
}
