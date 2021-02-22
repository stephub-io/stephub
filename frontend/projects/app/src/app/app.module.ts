import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { NgModule } from "@angular/core";
import { HttpClientModule } from "@angular/common/http";

import { CoreModule } from "./core/core.module";

import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app/app.component";
import { HIGHLIGHT_OPTIONS } from "ngx-highlightjs";
import { ACE_CONFIG, AceConfigInterface } from "ngx-ace-wrapper";
import { MarkdownModule } from "ngx-markdown";

const DEFAULT_ACE_CONFIG: AceConfigInterface = {};

@NgModule({
  imports: [
    // angular
    BrowserAnimationsModule,
    BrowserModule,
    HttpClientModule,

    // core
    CoreModule,

    // app
    AppRoutingModule,
    MarkdownModule.forRoot(),
  ],
  declarations: [AppComponent],
  bootstrap: [AppComponent],
  providers: [
    {
      provide: HIGHLIGHT_OPTIONS,
      useValue: {
        coreLibraryLoader: () => import("highlight.js/lib/core"),
        lineNumbersLoader: () => import("highlightjs-line-numbers.js"), // Optional, only if you want the line numbers
        languages: {
          json: () => import("highlight.js/lib/languages/json"),
        },
      },
    },
    {
      provide: ACE_CONFIG,
      useValue: DEFAULT_ACE_CONFIG,
    },
  ],
})
export class AppModule {}
