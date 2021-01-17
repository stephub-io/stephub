import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Execution, ExecutionsResult, ExecutionStart } from "./execution.model";
import { Observable } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class ExecutionService {
  constructor(private http: HttpClient) {}

  public fetch(wid: string): Observable<ExecutionsResult> {
    return this.http.get<ExecutionsResult>(
      `/api/v1/workspaces/${wid}/executions`
    );
  }

  public get(wid: string, id: string): Observable<Execution> {
    return this.http.get<Execution>(
      `/api/v1/workspaces/${wid}/executions/${id}`
    );
  }

  public start(wid: string, start: ExecutionStart): Observable<Execution> {
    return this.http.post<Execution>(
      `/api/v1/workspaces/${wid}/executions`,
      start
    );
  }
}
