import { environment as env } from "../../../environments/environment";

export interface Feature {
  name: string;
  version?: string;
  description: string;
  github?: string;
  documentation: string;
  medium?: string;
}

export const features: Feature[] = [
  {
    name: "Angular",
    version: env.versions.angular,
    description: "sh.features.angular",
    github: "https://github.com/angular/angular",
    documentation: "https://angular.io/docs/ts/latest/",
  },
  {
    name: "Angular Material",
    version: env.versions.material,
    description: "sh.features.angular-material",
    github: "https://github.com/angular/material2/",
    documentation: "https://material.angular.io/",
  },
  {
    name: "Angular Cli",
    version: env.versions.angularCli,
    description: "sh.features.angular-cli",
    github: "https://github.com/angular/angular-cli",
    documentation: "https://cli.angular.io/",
  },
  {
    name: "NgRx",
    version: env.versions.ngrx,
    description: "sh.features.ngrx",
    github: "https://github.com/ngrx/platform",
    documentation: "http://ngrx.github.io/",
    medium:
      "https://medium.com/@tomastrajan/object-assign-vs-object-spread-in-angular-ngrx-reducers-3d62ecb4a4b0",
  },
  {
    name: "RxJS",
    version: env.versions.rxjs,
    description: "sh.features.rxjs",
    github: "https://github.com/ReactiveX/RxJS",
    documentation: "http://reactivex.io/rxjs/",
    medium:
      "https://medium.com/@tomastrajan/practical-rxjs-in-the-wild-requests-with-concatmap-vs-mergemap-vs-forkjoin-11e5b2efe293",
  },
  {
    name: "Bootstrap",
    version: env.versions.bootstrap,
    description: "sh.features.bootstrap",
    github: "https://github.com/twbs/bootstrap",
    documentation: "https://getbootstrap.com/docs/4.0/layout/grid/",
    medium:
      "https://medium.com/@tomastrajan/how-to-build-responsive-layouts-with-bootstrap-4-and-angular-6-cfbb108d797b",
  },
  {
    name: "Typescript",
    version: env.versions.typescript,
    description: "sh.features.typescript",
    github: "https://github.com/Microsoft/TypeScript",
    documentation: "https://www.typescriptlang.org/docs/home.html",
  },
  {
    name: "I18n",
    version: env.versions.ngxtranslate,
    description: "sh.features.ngxtranslate",
    github: "https://github.com/ngx-translate/core",
    documentation: "http://www.ngx-translate.com/",
  },
  {
    name: "Font Awesome 5",
    version: env.versions.fontAwesome,
    description: "sh.features.fontawesome",
    github: "https://github.com/FortAwesome/Font-Awesome",
    documentation: "https://fontawesome.com/icons",
  },
  {
    name: "Cypress",
    version: env.versions.cypress,
    description: "sh.features.cypress",
    github: "https://github.com/cypress-io/cypress",
    documentation: "https://www.cypress.io/",
  },
  {
    name: "sh.features.themes.title",
    description: "sh.features.themes.description",
    documentation: "https://material.angular.io/guide/theming",
    medium:
      "https://medium.com/@tomastrajan/the-complete-guide-to-angular-material-themes-4d165a9d24d1",
  },
  {
    name: "sh.features.lazyloading.title",
    description: "sh.features.lazyloading.description",
    documentation:
      "https://angular.io/guide/router#lazy-loading-route-configuration",
  },
];
