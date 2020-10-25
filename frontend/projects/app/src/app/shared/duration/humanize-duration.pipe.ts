import { Pipe, PipeTransform } from "@angular/core";
import { parse, toSeconds } from "iso8601-duration";

const humanizeDuration = require("humanize-duration");

@Pipe({ name: "humanizeDuration" })
export class HumanizeDurationPipe implements PipeTransform {
  transform(value: string): string {
    if (value) {
      const ms = toSeconds(parse(value)) * 1000;
      return humanizeDuration(ms);
    } else {
      return value;
    }
  }
}
