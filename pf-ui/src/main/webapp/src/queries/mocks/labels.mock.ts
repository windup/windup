import { LabelDto } from "@app/api/label";

export let MOCK_LABELS: LabelDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const label1: LabelDto = {
    id: "label1",
    name: "Label1",
    description: "Label description",
    supported: ["tag1"],
    unsuitable: ["tag2"],
    neutral: ["tag3"],
  };

  MOCK_LABELS = [label1];
}
