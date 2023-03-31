import { FileDto } from "@app/api/file";
import { FileContentDto } from "@app/api/file-content";

export let MOCK_APP_FILES: FileDto[];
export let MOCK_APP_FILES_CONTENT: { [id: string]: FileContentDto } = {};

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const file1: FileDto = {
    id: "file-1",
    fullPath: "file.jar",
    prettyPath: "file.jar",
    prettyFileName: "file.jar",
    sourceType: "binary",
    storyPoints: 1,
    hints: [
      {
        line: 1,
        title: "Title",
        ruleId: "rule-1",
        content: "hint content",
        links: [],
      },
    ],
    tags: [],
    classificationsAndHintsTags: [],
  };

  MOCK_APP_FILES = [file1];
  MOCK_APP_FILES_CONTENT = {
    [file1.id]: { id: file1.id, content: "file content" },
  };
}
