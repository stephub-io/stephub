import { SpecSuggest } from "./spec-suggest";
import { StepSpec } from "../step.model";
import {
  StepInstruction,
  StepLine,
  StepLinePartAssignment,
  StepLinePartAttribute,
  StepLinePartKeyword,
  StepLinePartText,
} from "../parser/instruction-parser";
import { GherkinPreferences } from "../../workspace/workspace.model";

describe("Suggest", () => {
  it("should suggest nothing", () => {
    expect(
      new SpecSuggest({
        pattern: "I do something",
      } as StepSpec).completes("Given", "abc", null)
    ).toEqual([]);
  });

  it("should suggest without args and no assignment", () => {
    expect(
      new SpecSuggest({
        pattern: "I do something",
      } as StepSpec).completes("Given", "do", null)
    ).toEqual([
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" I do something"),
        ]),
      ]),
    ]);
  });

  it("should suggest without args and output", () => {
    expect(
      new SpecSuggest({
        pattern: "I do something",
        output: {},
      } as StepSpec).completes("Given", "do", null)
    ).toEqual([
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" I do something"),
        ]),
      ]),
    ]);
  });

  it("should suggest without args and two assignments", () => {
    expect(
      new SpecSuggest({
        pattern: "I do something",
        output: {},
      } as StepSpec).completes("Given", "do", {
        assignmentKeywords: ["- assigned to @ATTRIBUTE", "- to @ATTRIBUTE"],
      } as GherkinPreferences)
    ).toEqual([
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" I do something"),
          new StepLinePartAssignment(" - assigned to ", "${attribute}", ""),
        ]),
      ]),
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" I do something"),
          new StepLinePartAssignment(" - to ", "${attribute}", ""),
        ]),
      ]),
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" I do something"),
        ]),
      ]),
    ]);
  });

  it("should suggest with args and assignment", () => {
    expect(
      new SpecSuggest({
        pattern: "I do in {duration}s something",
        output: {},
      } as StepSpec).completes("Given", "do", {
        assignmentKeywords: ["- assigned to @ATTRIBUTE"],
      } as GherkinPreferences)
    ).toEqual([
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" I do in "),
          new StepLinePartAttribute("{duration}"),
          new StepLinePartText("s something"),
          new StepLinePartAssignment(" - assigned to ", "${attribute}", ""),
        ]),
      ]),
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" I do in "),
          new StepLinePartAttribute("{duration}"),
          new StepLinePartText("s something"),
        ]),
      ]),
    ]);
  });

  it("should suggest with tow args", () => {
    expect(
      new SpecSuggest({
        pattern: "I do in {duration}s and repeat {times} times something",
      } as StepSpec).completes("Given", "do", null)
    ).toEqual([
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" I do in "),
          new StepLinePartAttribute("{duration}"),
          new StepLinePartText("s and repeat "),
          new StepLinePartAttribute("{times}"),
          new StepLinePartText(" times something"),
        ]),
      ]),
    ]);
  });
});
