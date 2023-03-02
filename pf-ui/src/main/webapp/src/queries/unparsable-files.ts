import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationUnparsableFilesDto } from "@app/api/unparsable-files";

import { useMockableQuery } from "./helpers";
import { MOCK_UNPARSABLE_FILES } from "./mocks/unparsable-files.mock";

export const useUnparsableFilesQuery = (): UseQueryResult<
  ApplicationUnparsableFilesDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationUnparsableFilesDto[], AxiosError>(
    {
      queryKey: ["unparsable-files"],
      queryFn: async () =>
        (await axios.get<ApplicationUnparsableFilesDto[]>("/unparsable-files"))
          .data,
    },
    MOCK_UNPARSABLE_FILES,
    (window as any)["unparsable-files"]
  );
};
