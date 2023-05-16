import { TagDto } from "@app/api/tag";

export let MOCK_TAGS: TagDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const tag1: TagDto = {
    name: "tag1",
    title: "Tag1",
    isRoot: true,
    isPseudo: false,
    parentsTagNames: [],
  };

  MOCK_TAGS = [tag1];
}
