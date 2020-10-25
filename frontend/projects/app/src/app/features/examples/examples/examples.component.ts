import { Store, select } from "@ngrx/store";
import { Component, OnInit, ChangeDetectionStrategy } from "@angular/core";
import { Observable } from "rxjs";

import {
  routeAnimations,
  selectIsAuthenticated,
} from "../../../core/core.module";

import { State } from "../examples.state";

@Component({
  selector: "sh-examples",
  templateUrl: "./examples.component.html",
  styleUrls: ["./examples.component.scss"],
  animations: [routeAnimations],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExamplesComponent implements OnInit {
  isAuthenticated$: Observable<boolean>;

  examples = [
    { link: "todos", label: "sh.examples.menu.todos" },
    { link: "stock-market", label: "sh.examples.menu.stocks" },
    { link: "theming", label: "sh.examples.menu.theming" },
    { link: "crud", label: "sh.examples.menu.crud" },
    {
      link: "simple-state-management",
      label: "sh.examples.menu.simple-state-management",
    },
    { link: "form", label: "sh.examples.menu.form" },
    { link: "notifications", label: "sh.examples.menu.notifications" },
    { link: "elements", label: "sh.examples.menu.elements" },
    { link: "authenticated", label: "sh.examples.menu.auth", auth: true },
  ];

  constructor(private store: Store<State>) {}

  ngOnInit(): void {
    this.isAuthenticated$ = this.store.pipe(select(selectIsAuthenticated));
  }
}
