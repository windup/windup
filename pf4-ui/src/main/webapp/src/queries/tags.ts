import axios, { AxiosError } from "axios";

import { TagDto } from "@app/api/tag";

import { useMockableQuery } from "./helpers";
import { MOCK_TAGS } from "./mocks/tags.mock";

export const useTagsQuery = () => {
  return useMockableQuery<TagDto[], AxiosError>(
    {
      queryKey: ["tags"],
      queryFn: async () => (await axios.get<TagDto[]>("/tags")).data,
    },
    MOCK_TAGS,
    (window as any)["tags"]
  );
};
