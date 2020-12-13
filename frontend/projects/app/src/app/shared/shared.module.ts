import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TranslateModule } from "@ngx-translate/core";

import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";
import { MatSelectModule } from "@angular/material/select";
import { MatTabsModule } from "@angular/material/tabs";
import { MatInputModule } from "@angular/material/input";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatChipsModule } from "@angular/material/chips";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatCardModule } from "@angular/material/card";
import { MatListModule } from "@angular/material/list";
import { MatIconModule } from "@angular/material/icon";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { MatDividerModule } from "@angular/material/divider";
import { MatSliderModule } from "@angular/material/slider";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatToolbarModule } from "@angular/material/toolbar";
import { NgxSkeletonLoaderModule } from "ngx-skeleton-loader";

import {
  FaIconLibrary,
  FontAwesomeModule,
} from "@fortawesome/angular-fontawesome";
import {
  faAngleRight,
  faBook,
  faBox,
  faCaretDown,
  faCaretUp,
  faCheck,
  faCheckCircle,
  faEdit,
  faExclamationTriangle,
  faFilter,
  faGripVertical,
  faLanguage,
  faLightbulb,
  faMagic,
  faMinusCircle,
  faPaintBrush,
  faPlus,
  faReceipt,
  faRocket,
  faSquare,
  faStream,
  faTasks,
  faTimes,
  faTimesCircle,
  faTrash,
  faWindowMaximize,
} from "@fortawesome/free-solid-svg-icons";
import {
  faGithub,
  faMediumM,
  faSearchengin,
} from "@fortawesome/free-brands-svg-icons";

import { BigInputComponent } from "./big-input/big-input/big-input.component";
import { BigInputActionComponent } from "./big-input/big-input-action/big-input-action.component";
import { RtlSupportDirective } from "./rtl-support/rtl-support.directive";
import { HumanizeDurationPipe } from "./duration/humanize-duration.pipe";
import { TypeofPipe } from "./typeof/typeof.pipe";
import { JsonViewComponent } from "./json-view/json-view.component";
import { BreadcrumbModule } from "xng-breadcrumb";
import { HighlightModule } from "ngx-highlightjs";
import { EmptyStateComponent } from "./empty-state/empty-state.component";
import { AceModule } from "ngx-ace-wrapper";
import { JsonSchemaViewComponent } from "./json-schema-view/json--schema-view.component";
import { HoverClassDirective } from "./hover/hover-class.directive";
import { FocusDirective } from "./focus/focus.directive";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,

    TranslateModule,

    MatButtonModule,
    MatSelectModule,
    MatTabsModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatCardModule,
    MatCheckboxModule,
    MatListModule,
    MatMenuModule,
    MatIconModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatSlideToggleModule,
    MatDividerModule,
    MatToolbarModule,

    NgxSkeletonLoaderModule,

    FontAwesomeModule,

    BreadcrumbModule,
    HighlightModule,
    AceModule,
  ],
  declarations: [
    BigInputComponent,
    BigInputActionComponent,
    RtlSupportDirective,
    HumanizeDurationPipe,
    TypeofPipe,
    EmptyStateComponent,
    JsonViewComponent,
    JsonSchemaViewComponent,
    HoverClassDirective,
    FocusDirective,
  ],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,

    TranslateModule,

    MatButtonModule,
    MatMenuModule,
    MatTabsModule,
    MatChipsModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    MatCardModule,
    MatListModule,
    MatSelectModule,
    MatIconModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatSlideToggleModule,
    MatDividerModule,
    MatSliderModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatToolbarModule,

    FontAwesomeModule,

    NgxSkeletonLoaderModule,

    BreadcrumbModule,
    HighlightModule,
    AceModule,

    BigInputComponent,
    BigInputActionComponent,
    RtlSupportDirective,
    HumanizeDurationPipe,
    TypeofPipe,
    EmptyStateComponent,
    JsonViewComponent,
    JsonSchemaViewComponent,
    HoverClassDirective,
    FocusDirective,
  ],
})
export class SharedModule {
  constructor(faIconLibrary: FaIconLibrary) {
    faIconLibrary.addIcons(
      faGithub,
      faMediumM,
      faPlus,
      faEdit,
      faTrash,
      faTimes,
      faCaretUp,
      faCaretDown,
      faExclamationTriangle,
      faFilter,
      faTasks,
      faCheck,
      faSquare,
      faLanguage,
      faPaintBrush,
      faLightbulb,
      faWindowMaximize,
      faStream,
      faBook,
      faBox,
      faRocket,
      faReceipt,
      faMagic,
      faSearchengin,
      faMinusCircle,
      faAngleRight,
      faGripVertical,
      faCheckCircle,
      faTimesCircle
    );
  }
}
