import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {
  Execution,
  ExecutionLogAttachment,
  ExecutionStart,
  ExecutionType,
  LoadScenarioRun,
  PageResult,
} from "./execution.model";
import { Observable } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class ExecutionService {
  constructor(private http: HttpClient) {}

  public fetch<E extends Execution>(
    wid: string,
    type?: ExecutionType
  ): Observable<PageResult<E>> {
    return this.http.get<PageResult<E>>(
      `/api/v1/workspaces/${wid}/executions` + (type ? "?type=" + type : "")
    );
  }

  public get<E extends Execution>(wid: string, id: string): Observable<E> {
    return this.http.get<E>(`/api/v1/workspaces/${wid}/executions/${id}`);
  }

  public stop<E extends Execution>(wid: string, id: string): Observable<E> {
    return this.http.post<E>(
      `/api/v1/workspaces/${wid}/executions/${id}:stop`,
      null
    );
  }

  public fetchLoadRuns(
    wid: string,
    execId: string,
    simId: string,
    offset = 0,
    size = 25
  ): Observable<PageResult<LoadScenarioRun>> {
    return this.http.get<PageResult<LoadScenarioRun>>(
      `/api/v1/workspaces/${wid}/executions/${execId}/loadRuns?simId=${simId}&offset=${offset}&size=${size}`
    );
  }

  public start<E extends Execution>(
    wid: string,
    start: ExecutionStart
  ): Observable<E> {
    return this.http.post<E>(`/api/v1/workspaces/${wid}/executions`, start);
  }

  getAttachmentHref(wid: string, execId: string, a: ExecutionLogAttachment) {
    return `/api/v1/workspaces/${wid}/executions/${execId}/attachments/${a.id}`;
  }
}
