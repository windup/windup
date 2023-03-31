import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationCompatibleFilesDto } from "@app/api/compatible-files";

import { useMockableQuery } from "./helpers";
import { MOCK_COMPATIBLE_FILES } from "./mocks/compatible-files.mock";

export const useCompatibleFilesQuery = (): UseQueryResult<
  ApplicationCompatibleFilesDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationCompatibleFilesDto[], AxiosError>(
    {
      queryKey: ["compatible-files"],
      queryFn: async () =>
        (await axios.get<ApplicationCompatibleFilesDto[]>("/compatible-files"))
          .data,
    },
    MOCK_COMPATIBLE_FILES,
    (window as any)["compatible-files"]
  );
};
