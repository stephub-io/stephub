import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { WorkspacesResult, Workspace } from "./workspace.model";
import { Observable } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class WorkspaceService {
  constructor(private http: HttpClient) {}

  public fetch(): Observable<WorkspacesResult> {
    return this.http.get<WorkspacesResult>("/api/v1/workspaces");
  }

  public get(id: string): Observable<Workspace> {
    return this.http.get<Workspace>(`/api/v1/workspaces/${id}`);
  }

  patch(id: string, patch: Workspace) {
    return this.http.patch<Workspace>(`/api/v1/workspaces/${id}`, patch);
  }
}
