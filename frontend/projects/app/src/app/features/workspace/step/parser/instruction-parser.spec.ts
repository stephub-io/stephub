import {
  DataTable,
  DocString,
  parse,
  StepInstruction,
  StepLine,
  StepLinePartAssignment,
  StepLinePartAttribute,
  StepLinePartKeyword,
  StepLinePartText,
} from "./instruction-parser";
import { StepSpec } from "../step.model";

describe("Parse instruction", () => {
  it("should parse line as is without prefs", () => {
    expect(parse("Hello step", null, false, null)).toEqual(
      new StepInstruction([new StepLine([new StepLinePartText("Hello step")])])
    );
  });

  it("should parse keyword", () => {
    expect(
      parse("given i say hello", null, false, {
        stepKeywords: ["Given"],
        assignmentKeywords: [],
      })
    ).toEqual(
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("given"),
          new StepLinePartText(" i say hello"),
        ]),
      ])
    );
  });

  it("should parse keyword with one arg", () => {
    expect(
      parse(
        'given i say "hello" and ok',
        {
          pattern: "I say {text} and ok",
        } as StepSpec,
        false,
        {
          stepKeywords: ["Given"],
          assignmentKeywords: [],
        }
      )
    ).toEqual(
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("given"),
          new StepLinePartText(" i say "),
          new StepLinePartAttribute('"hello"'),
          new StepLinePartText(" and ok"),
        ]),
      ])
    );
  });

  it("should parse keyword and assignment", () => {
    expect(
      parse("given a value - assigned to ${var}!", null, false, {
        stepKeywords: ["Given"],
        assignmentKeywords: ["- assigned to @ATTRIBUTE!"],
      })
    ).toEqual(
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("given"),
          new StepLinePartText(" a value "),
          new StepLinePartAssignment("- assigned to ", "${var}", "!"),
        ]),
      ])
    );
  });

  it("should parse keyword, two args and assignment", () => {
    expect(
      parse(
        'given a value 7 and "abc" - assigned to ${var}!',
        {
          pattern: "a value {v1} and {v2}",
        } as StepSpec,
        false,
        {
          stepKeywords: ["Given"],
          assignmentKeywords: ["- assigned to @ATTRIBUTE!"],
        }
      )
    ).toEqual(
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("given"),
          new StepLinePartText(" a value "),
          new StepLinePartAttribute("7"),
          new StepLinePartText(" and "),
          new StepLinePartAttribute('"abc"'),
          new StepLinePartText(" "),
          new StepLinePartAssignment("- assigned to ", "${var}", "!"),
        ]),
      ])
    );
  });

  it("should parse longest keyword and assignment", () => {
    expect(
      parse("given i have a value - assigned to ${var}", null, false, {
        stepKeywords: ["Given", "Given I"],
        assignmentKeywords: [
          "assigned to @ATTRIBUTE",
          "- assigned to @ATTRIBUTE",
        ],
      })
    ).toEqual(
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("given i"),
          new StepLinePartText(" have a value "),
          new StepLinePartAssignment("- assigned to ", "${var}", ""),
        ]),
      ])
    );
  });

  it("should parse two args", () => {
    expect(
      parse(
        "Given assert that ${var.var1} equals ${my.hello}",
        {
          pattern: "assert that {actual} equals {expected}",
        } as StepSpec,
        false,
        {
          stepKeywords: ["Given"],
          assignmentKeywords: ["- assigned to @ATTRIBUTE"],
        }
      )
    ).toEqual(
      new StepInstruction([
        new StepLine([
          new StepLinePartKeyword("Given"),
          new StepLinePartText(" assert that "),
          new StepLinePartAttribute("${var.var1}"),
          new StepLinePartText(" equals "),
          new StepLinePartAttribute("${my.hello}"),
        ]),
      ])
    );
  });

  it("should parse docString", () => {
    expect(
      parse(
        "Given something:\n" + '"""\n' + "Line 1\n" + "Line 2\n" + '"""',
        null,
        false,
        null
      )
    ).toEqual(
      new StepInstruction([
        new StepLine([new StepLinePartText("Given something:")]),
        new DocString(["Line 1", "Line 2"]),
      ])
    );
  });

  it("should parse dataTable", () => {
    expect(
      parse(
        "Given something:\n" + " | Col 1 | Col 2 | \n" + " | A     | B     |\n",
        null,
        false,
        null
      )
    ).toEqual(
      new StepInstruction([
        new StepLine([new StepLinePartText("Given something:")]),
        new DataTable(["| Col 1 | Col 2 |", "| A     | B     |"]),
      ])
    );
  });
});
