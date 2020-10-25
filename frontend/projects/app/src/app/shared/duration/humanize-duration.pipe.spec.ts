import { HumanizeDurationPipe } from "./humanize-duration.pipe";

describe("HumanizeDurationPipe", () => {
  let pipe: HumanizeDurationPipe;

  beforeEach(() => {
    pipe = new HumanizeDurationPipe();
  });

  it("should render 0s", () => {
    expect(pipe.transform("PT0S")).toContain("0 seconds");
  });

  it("should render 0.002s", () => {
    expect(pipe.transform("PT0.002S")).toEqual("0.002 seconds");
  });

  it("should render 1 minute", () => {
    expect(pipe.transform("PT1M")).toEqual("1 minute");
  });
});
