import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { ProviderInfo } from "./provider.model";
import { ProviderSpec } from "../workspace/workspace.model";

@Injectable({
  providedIn: "root",
})
export class ProviderService {
  constructor(private http: HttpClient) {}

  public getRegistered(): Observable<ProviderInfo[]> {
    return this.http.get<ProviderInfo[]>(`/api/v1/providers/registered`);
  }

  public getProviderInfo(spec: ProviderSpec): Observable<ProviderInfo> {
    return this.http.post<ProviderInfo>(`/api/v1/providers/lookup`, spec);
  }
}
