import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  QueryList,
  ViewChildren,
} from "@angular/core";
import "brace";
import "brace/mode/json";
import "brace/theme/github";
import {
  Feature,
  Scenario,
  StepsCollection,
  Workspace,
} from "../../workspace/workspace.model";
import {
  faMagic,
  faReceipt,
  faRocket,
} from "@fortawesome/free-solid-svg-icons";
import { MatInput } from "@angular/material/input";
import { Validators } from "@angular/forms";
import {
  parse,
  StepInstruction,
  StepLine,
  StepLinePartKeyword,
} from "../../step/parser/instruction-parser";
import { SpecSuggest } from "../../step/spec-suggest/spec-suggest";
import { SuggestGroup, SuggestOption } from "../../../../util/auto-suggest";
import { MatDialog, MatDialogConfig } from "@angular/material/dialog";
import { StepBrowserDialogComponent } from "../../step/step-browser-dialog/step-browser-dialog.component";

@Component({
  selector: "sh-workspace-features",
  templateUrl: "./workspace-features.component.html",
  styleUrls: ["./workspace-features.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkspaceFeaturesComponent implements OnChanges {
  featureIcon = faRocket;
  scenarioIcon = faReceipt;
  stepIcon = faMagic;

  private specSuggests: Map<string, SpecSuggest[]> = new Map<
    string,
    SpecSuggest[]
  >();

  @Input() workspace: Workspace;
  @Input() editMode = false;
  @Input() stepsCollection: StepsCollection;

  @ViewChildren("stepEdit") stepEditFields: QueryList<MatInput>;

  validatorRequired = Validators.required;
  validatorTagsLine = Validators.pattern(/^(\s*@[^#@\s]+(\s+@[^#@\s]+)*\s*)?$/);

  stepAutoCompleteSource: (value: string) => SuggestGroup[] = (value) =>
    this.autoCompleteFilter(value);
  private instructionCache = new Map<string, StepInstruction>();

  constructor(private dialog: MatDialog) {}

  ngOnChanges() {
    console.log("Clearing instructions cache");
    this.specSuggests.clear();
    if (this.stepsCollection) {
      for (const providerName in this.stepsCollection) {
        const provSuggests = [];
        this.stepsCollection[providerName]
          .map((spec) => new SpecSuggest(spec))
          .forEach((s) => provSuggests.push(s));
        this.specSuggests.set(providerName, provSuggests);
      }
    }
    this.instructionCache.clear();
  }

  splitTags(tagsString: string) {
    if (tagsString.trim().length > 0) {
      return tagsString.trim().split(/\s+/);
    } else {
      return [];
    }
  }

  deleteFeature(fi: number) {
    this.workspace.features.splice(fi, 1);
    this.workspace.features = [...this.workspace.features];
  }

  deleteScenario(feature: Feature, si: number) {
    feature.scenarios.splice(si, 1);
    feature.scenarios = [...feature.scenarios];
  }

  addScenario(feature: Feature) {
    feature.scenarios.push({
      name: "New scenario",
      steps: [],
    } as Scenario);
  }

  addFeature() {
    this.workspace.features.push({
      name: "New feature",
      scenarios: [],
      background: {
        steps: [],
      },
    } as Feature);
  }

  private autoCompleteFilter(value: string): SuggestGroup[] {
    const filterValue = value.trim().toLowerCase();
    if (filterValue.indexOf("\n") >= 0) {
      return [];
    }
    const suggestionGroups: SuggestGroup[] = [];
    // Check containment of step keywords
    const stepKeywords = this.workspace.gherkinPreferences.stepKeywords;
    const usedKeywords = stepKeywords.filter((key) =>
      filterValue.startsWith(key.toLowerCase())
    );

    // Keyword not used, suggest keywords
    const keywordSuggest = stepKeywords.filter((option) =>
      option.toLowerCase().includes(filterValue)
    );
    if (keywordSuggest.length > 0) {
      suggestionGroups.push({
        label: "Keywords",
        options: keywordSuggest.map((s) => {
          return {
            value: s,
            view: new StepInstruction([
              new StepLine([new StepLinePartKeyword(s)]),
            ]),
          } as SuggestOption;
        }),
      } as SuggestGroup);
    }
    // Combination composed of keyword and step
    if (usedKeywords.length > 0) {
      usedKeywords.forEach((keyword) => {
        const rest = value.substr(keyword.length);
        this.specSuggests.forEach((specSuggests, pName) => {
          const provSuggestionOptions = [];
          specSuggests.forEach((specSuggest) => {
            specSuggest
              .completes(keyword, rest, this.workspace.gherkinPreferences)
              .forEach((instruction) =>
                provSuggestionOptions.push({
                  view: instruction,
                  value: instruction.toString(),
                })
              );
          });
          if (provSuggestionOptions.length > 0) {
            suggestionGroups.push({
              label: "Provider - " + pName,
              options: provSuggestionOptions,
            } as SuggestGroup);
          }
        });
      });
    }
    return suggestionGroups;
  }

  instruction(step: string): StepInstruction {
    let instruction = this.instructionCache.get(step);
    if (instruction) {
      return instruction;
    }
    if (this.stepsCollection) {
      for (const providerName in this.stepsCollection) {
        for (let i = 0; i < this.stepsCollection[providerName].length; i++) {
          instruction = parse(
            step,
            this.stepsCollection[providerName][i],
            true,
            this.workspace.gherkinPreferences
          );
          if (instruction.matchingSpec) {
            this.instructionCache.set(step, instruction);
            return instruction;
          }
        }
      }
    }
    instruction = parse(step, null, false, this.workspace.gherkinPreferences);
    this.instructionCache.set(step, instruction);
    return instruction;
  }

  openStepSpec(stepInstruction: StepInstruction, $event: MouseEvent) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = false;
    dialogConfig.data = {
      stepsCollection: this.stepsCollection,
      filterBySpec: stepInstruction.matchingSpec,
    };
    dialogConfig.width = "80%";
    dialogConfig.minHeight = "80%";

    this.dialog.open(StepBrowserDialogComponent, dialogConfig);
    $event.preventDefault();
  }
}

export function initFeatures(workspace: Workspace) {
  if (!workspace.features) {
    workspace.features = [];
  }
  workspace.features.forEach((feature) => {
    if (!feature.background) {
      feature.background = {
        steps: [],
      };
    }
    if (!feature.scenarios) {
      feature.scenarios = [];
    }
    feature.scenarios.forEach((scenario) => {
      if (!scenario.steps) {
        scenario.steps = [];
      }
    });
  });
}
