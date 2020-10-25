import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";

import { WorkspaceListComponent } from "./workspace-list/workspace-list.component";
import { WorkspaceComponent } from "./workspace/workspace.component";
import { ExecutionListComponent } from "./execution-list/execution-list.component";
import { WorkspaceDetailComponent } from "./workspace-detail/workspace-detail.component";
import { ExecutionDetailComponent } from "./execution-detail/execution-detail.component";

const routes: Routes = [
  {
    path: "",
    component: WorkspaceListComponent,
    data: { title: "sh.menu.workspaces" },
  },
  {
    path: ":wid",
    component: WorkspaceComponent,
    children: [
      {
        path: "",
        component: WorkspaceDetailComponent,
      },
      {
        path: "executions",
        component: ExecutionListComponent,
      },
      {
        path: "executions/:id",
        component: ExecutionDetailComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WorkspacesRoutingModule {}
