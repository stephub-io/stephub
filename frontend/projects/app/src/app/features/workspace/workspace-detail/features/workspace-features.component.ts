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
import { SuggestOption } from "../../../../shared/multi-string-input/multi-string-input.component";
import {
  parse,
  StepInstruction,
  StepLine,
  StepLinePartKeyword,
} from "../../step/parser/instruction-parser";
import { SpecSuggest } from "../../step/spec-suggest/spec-suggest";

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

  private specSuggests: SpecSuggest[] = [];

  @Input() workspace: Workspace;
  @Input() editMode = false;
  @Input() stepsCollection: StepsCollection;

  @ViewChildren("stepEdit") stepEditFields: QueryList<MatInput>;

  validatorRequired = Validators.required;
  validatorTagsLine = Validators.pattern(/^(\s*@[^#@\s]+(\s+@[^#@\s]+)*\s*)?$/);

  stepAutoCompleteSource: (value: string) => SuggestOption[] = (value) =>
    this.autoCompleteFilter(value);
  private instructionCache = new Map<string, StepInstruction>();

  constructor() {}

  ngOnChanges() {
    this.specSuggests = [];
    if (this.stepsCollection) {
      for (const providerName in this.stepsCollection) {
        this.stepsCollection[providerName]
          .map((spec) => new SpecSuggest(spec))
          .forEach((s) => this.specSuggests.push(s));
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

  private autoCompleteFilter(value: string): SuggestOption[] {
    const filterValue = value.trim().toLowerCase();
    if (filterValue.indexOf("\n") >= 0) {
      return [];
    }
    const suggestionOptions: SuggestOption[] = [];
    // Check containment of step keywords
    const stepKeywords = this.workspace.gherkinPreferences.stepKeywords;
    const usedKeywords = stepKeywords.filter((key) =>
      filterValue.startsWith(key.toLowerCase())
    );

    // Keyword not used, suggest keywords
    stepKeywords
      .filter((option) => option.toLowerCase().includes(filterValue))
      .forEach((s) => {
        suggestionOptions.push({
          value: s,
          view: new StepInstruction([
            new StepLine([new StepLinePartKeyword(s)]),
          ]),
        } as SuggestOption);
      });
    // Combination composed of keyword and step
    if (usedKeywords.length > 0) {
      usedKeywords.forEach((keyword) => {
        const rest = value.substr(keyword.length);
        this.specSuggests.forEach((specSuggest) => {
          specSuggest
            .completes(keyword, rest, this.workspace.gherkinPreferences)
            .forEach((instruction) =>
              suggestionOptions.push({
                view: instruction,
                value: instruction.toString(),
              })
            );
        });
      });
    }
    return suggestionOptions;
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
