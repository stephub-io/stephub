import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { RegisteredProvider } from "./provider.model";

@Injectable({
  providedIn: "root",
})
export class ProviderService {
  constructor(private http: HttpClient) {}

  public getRegistered(): Observable<RegisteredProvider[]> {
    return this.http.get<RegisteredProvider[]>(`/api/v1/providers/registered`);
  }
}
