import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";

import { SharedModule } from "../../shared/shared.module";
import { WorkspaceListComponent } from "./workspace-list/workspace-list.component";
import { WorkspaceDetailComponent } from "./workspace-detail/workspace-detail.component";
import { WorkspacesRoutingModule } from "./workspace-routing.module";
import { WorkspaceSubnavComponent } from "./workspace-subnav/workspace-subnav.component";
import { ExecutionListComponent } from "./execution-list/execution-list.component";
import { WorkspaceComponent } from "./workspace/workspace.component";
import { ExecutionDetailComponent } from "./execution-detail/execution-detail.component";
import { MatExpansionModule } from "@angular/material/expansion";
import { ExecutionStatsComponent } from "./execution-stats/execution-stats.component";
import { StepAbstractComponent } from "./step/step-abstract/step-abstract.component";
import { StepRequestComponent } from "./step/step-request/step-request.component";
import { MatTableModule } from "@angular/material/table";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatDialogModule } from "@angular/material/dialog";
import { VariableDialogComponent } from "./workspace-detail/variable-dialog/variable-dialog.component";
import { MatSelectModule } from "@angular/material/select";
import { MatRadioModule } from "@angular/material/radio";
import { WorkspaceFeaturesComponent } from "./workspace-detail/features/workspace-features.component";
import { DragDropModule } from "@angular/cdk/drag-drop";

@NgModule({
  declarations: [
    WorkspaceListComponent,
    WorkspaceComponent,
    WorkspaceDetailComponent,
    WorkspaceSubnavComponent,
    ExecutionListComponent,
    ExecutionDetailComponent,
    ExecutionStatsComponent,
    StepAbstractComponent,
    StepRequestComponent,
    VariableDialogComponent,
    WorkspaceFeaturesComponent,
  ],
  imports: [
    CommonModule,
    SharedModule,
    WorkspacesRoutingModule,
    MatExpansionModule,
    MatTableModule,
    MatSidenavModule,
    MatDialogModule,
    MatSelectModule,
    MatRadioModule,
    DragDropModule,
  ],
})
export class WorkspaceModule {}
