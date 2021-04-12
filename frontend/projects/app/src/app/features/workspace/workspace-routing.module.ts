import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";

import { WorkspaceListComponent } from "./workspace-list/workspace-list.component";
import { WorkspaceComponent } from "./workspace/workspace.component";
import { ExecutionListComponent } from "./execution-list/execution-list.component";
import { WorkspaceDetailComponent } from "./workspace-detail/workspace-detail.component";
import { ExecutionDetailFunctionalComponent } from "./execution-detail/functional/execution-detail-functional.component";
import { ExecutionNewComponent } from "./execution-new/execution-new.component";
import { WorkspaceNewComponent } from "./workspace-new/workspace-new.component";
import { ExecutionType } from "./execution.model";
import { ExecutionDetailLoadComponent } from "./execution-detail/load/execution-detail-load.component";

const routes: Routes = [
  {
    path: "",
    component: WorkspaceListComponent,
    data: { title: "sh.menu.workspaces", breadcrumb: "Workspaces" },
  },
  {
    path: "new",
    component: WorkspaceNewComponent,
    data: { title: "New workspace", breadcrumb: "Create new..." },
  },
  {
    path: ":wid",
    component: WorkspaceComponent,
    children: [
      {
        path: "",
        component: WorkspaceDetailComponent,
        data: {
          breadcrumb: {
            alias: "workspace",
          },
        },
      },
      {
        path: "executions",
        data: {
          breadcrumb: "Executions",
        },
        children: [
          {
            path: "",
            component: ExecutionListComponent,
            data: {
              type: ExecutionType.FUNCTIONAL,
            },
          },
          {
            path: "new",
            component: ExecutionNewComponent,
            data: {
              breadcrumb: "Start execution...",
            },
          },
          {
            path: ":id",
            component: ExecutionDetailFunctionalComponent,
            data: {
              breadcrumb: {
                alias: "execution",
              },
            },
          },
        ],
      },
      {
        path: "loadExecutions",
        data: {
          breadcrumb: "Load tests",
        },
        children: [
          {
            path: "",
            component: ExecutionListComponent,
            data: {
              type: ExecutionType.LOAD,
            },
          },
          {
            path: "new",
            component: ExecutionNewComponent,
            data: {
              breadcrumb: "Start load test...",
            },
          },
          {
            path: ":id",
            component: ExecutionDetailLoadComponent,
            data: {
              breadcrumb: {
                alias: "execution",
              },
            },
          },
        ],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WorkspacesRoutingModule {}
