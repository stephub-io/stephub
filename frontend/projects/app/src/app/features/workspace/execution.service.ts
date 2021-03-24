import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {
  Execution,
  ExecutionLogAttachment,
  ExecutionsResult,
  ExecutionStart,
  ExecutionType,
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
  ): Observable<ExecutionsResult<E>> {
    return this.http.get<ExecutionsResult<E>>(
      `/api/v1/workspaces/${wid}/executions` + (type ? "?type=" + type : "")
    );
  }

  public get<E extends Execution>(wid: string, id: string): Observable<E> {
    return this.http.get<E>(`/api/v1/workspaces/${wid}/executions/${id}`);
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
