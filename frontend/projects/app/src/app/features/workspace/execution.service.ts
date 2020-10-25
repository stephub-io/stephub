import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { ExecutionsResult, Execution } from "./execution.model";
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
}
