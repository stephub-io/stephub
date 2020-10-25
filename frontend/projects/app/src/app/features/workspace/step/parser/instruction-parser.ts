import { GherkinPreferences } from "../../workspace/workspace.model";
import { StepSpec } from "../step.model";

enum FragmentStatus {
  none,
  started,
  ended,
}

export function parse(
  text: string,
  spec: StepSpec,
  strict: boolean,
  preferences: GherkinPreferences
): StepInstruction {
  let fragments = [];
  let stepLine = FragmentStatus.none;
  let docString = {
    status: FragmentStatus.none,
  } as DocStringBucket;
  let dataTable = {
    status: FragmentStatus.none,
  } as FragmentBucket<DataTable>;

  text
    .trim()
    .split(/[\n\r]+/)
    .forEach((value) => {
      const line = value.trim();
      if (
        docString.status == FragmentStatus.none &&
        dataTable.status == FragmentStatus.none
      ) {
        if (line.length == 0) {
          return;
        } else if (line.startsWith("#")) {
          fragments.push(new CommentLine(line));
          return;
        }
      }
      if (stepLine == FragmentStatus.none) {
        fragments.push(parseStepLine(line, spec, strict, preferences));
        stepLine = FragmentStatus.ended;
      } else if (
        docString.status == FragmentStatus.none &&
        line.startsWith('"""')
      ) {
        if (stepLine != FragmentStatus.ended) {
          fragments.push(
            new ErroneousLine(
              line,
              "Step expression required before Doc String"
            )
          );
        } else if (dataTable.status != FragmentStatus.none) {
          dataTable.status = FragmentStatus.ended;
          fragments.push(
            new ErroneousLine(
              line,
              "Mixing Doc String with Data Table not allowed"
            )
          );
        } else {
          docString.status = FragmentStatus.started;
          docString.offset = value.indexOf('"""');
          docString.payload = new DocString([]);
          fragments.push(docString.payload);
        }
      } else if (
        docString.status == FragmentStatus.started &&
        line.startsWith('"""')
      ) {
        docString.status = FragmentStatus.ended;
      } else if (docString.status == FragmentStatus.started) {
        docString.payload.lines.push(
          extractSpaceOffset(value, docString.offset)
        );
      } else if (
        dataTable.status == FragmentStatus.none &&
        line.startsWith("|")
      ) {
        if (stepLine != FragmentStatus.ended) {
          fragments.push(
            new ErroneousLine(
              line,
              "Step expression required before Data Table"
            )
          );
        } else {
          dataTable.status = FragmentStatus.started;
          dataTable.payload = new DataTable([line]);
          fragments.push(dataTable.payload);
        }
      } else if (dataTable.status == FragmentStatus.started) {
        if (line.startsWith("|") || line.startsWith("#") || line.length == 0) {
          dataTable.payload.rows.push(line);
        } else {
          dataTable.status = FragmentStatus.ended;
          fragments.push(
            new ErroneousLine(line, "Invalid row syntax in Data Table")
          );
        }
      } else {
        fragments.push(new ErroneousLine(line, "Invalid line sequence"));
      }
    });
  return new StepInstruction(fragments);
}

function parseStepLine(
  line: string,
  spec: StepSpec,
  strict: boolean,
  preferences: GherkinPreferences
): StepLine {
  let stepLine = new StepLine([]);
  if (preferences) {
    let stepPattern =
      "^(" +
      preferences.stepKeywords
        .sort((a, b) => b.length - a.length)
        .map((value) => escapeRegExp(value))
        .join("|") +
      ")";
    if (spec && spec.pattern) {
      stepPattern += spec.pattern
        .split(/(?<=^|[^\\])\{(?:[a-zA-Z_][a-zA-Z0-9_]*)\}/)
        .map(
          (value, index) =>
            "(\\s" + (index == 0 ? "+" : "*") + escapeRegExp(value) + "\\s*)"
        )
        .join("(.+?)");
    } else {
      stepPattern += "(.+?)";
    }
    stepPattern +=
      "(" +
      preferences.assignmentKeywords
        .map((value) =>
          value
            .split("@ATTRIBUTE")
            .map((v) => escapeRegExp(v))
            .join("\\${[^}]+}")
        )
        .join("|") +
      ")?$";
    const stepMatcher = RegExp(stepPattern, "gi").exec(line);
    if (stepMatcher) {
      stepLine.parts.push(new StepLinePartKeyword(stepMatcher[1]));
      for (let i = 2; i < stepMatcher.length - 1; i++) {
        if (i % 2 == 0) {
          if (stepMatcher[i].length > 0) {
            stepLine.parts.push(new StepLinePartText(stepMatcher[i]));
          }
        } else {
          stepLine.parts.push(new StepLinePartAttribute(stepMatcher[i]));
        }
      }
      // Assigment
      let ll = stepMatcher[stepMatcher.length - 1];
      for (let i = 0; i < preferences.assignmentKeywords.length; i++) {
        const keywordParts = preferences.assignmentKeywords[i]
          .toLowerCase()
          .split("@attribute");
        const r = new RegExp(
          "^(" +
            escapeRegExp(keywordParts[0]) +
            ")" +
            "(\\${[^}]+})" +
            "(" +
            escapeRegExp(keywordParts[1]) +
            ")$"
        );
        let match = r.exec(ll);
        if (match) {
          stepLine.parts.push(
            new StepLinePartAssignment(match[1], match[2], match[3])
          );
          break;
        }
      }
    } else {
      stepLine.parts.push(new StepLinePartText(line));
    }
  } else {
    stepLine.parts.push(new StepLinePartText(line));
  }
  return stepLine;
}

function escapeRegExp(string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function extractSpaceOffset(str: string, maxOffset: number) {
  maxOffset = Math.min(str.length, maxOffset);
  for (let i = 0; i < maxOffset; i++) {
    if (str.charAt(i) != " ") {
      return str.substring(i);
    }
  }
  return str.substring(maxOffset);
}

export class StepInstruction {
  fragments: Fragment[];

  constructor(fragments: Fragment[]) {
    this.fragments = fragments;
  }
}

export interface Fragment {}

export class CommentLine implements Fragment {
  comment: string;

  constructor(comment: string) {
    this.comment = comment;
  }
}

export class StepLine implements Fragment {
  parts: StepLinePart[];

  constructor(parts: StepLinePart[]) {
    this.parts = parts;
  }
}

export class ErroneousLine implements Fragment {
  text: string;
  errorMessage: string;

  constructor(text: string, errorMessage: string) {
    this.text = text;
    this.errorMessage = errorMessage;
  }
}

export interface StepLinePart {}

export class StepLinePartKeyword implements StepLinePart {
  keyword: string;

  constructor(keyword: string) {
    this.keyword = keyword;
  }
}

export class StepLinePartText implements StepLinePart {
  text: string;

  constructor(text: string) {
    this.text = text;
  }
}

export class StepLinePartAttribute implements StepLinePart {
  expression: string;

  constructor(expression: string) {
    this.expression = expression;
  }
}

export class StepLinePartAssignment implements StepLinePart {
  left: string;
  attribute: string;
  right: string;

  constructor(left: string, attribute: string, right: string) {
    this.left = left;
    this.attribute = attribute;
    this.right = right;
  }
}

export class DocString implements Fragment {
  lines: string[];

  constructor(lines: string[]) {
    this.lines = lines;
  }
}

export class DataTable implements Fragment {
  rows: string[];

  constructor(rows: string[]) {
    this.rows = rows;
  }
}

interface FragmentBucket<T extends Fragment> {
  status: FragmentStatus;
  payload: T;
}

interface DocStringBucket extends FragmentBucket<DocString> {
  offset: number;
}
