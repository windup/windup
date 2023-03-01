import { useCallback } from "react";

import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationIgnoredFilesDto } from "@app/api/ignored-files";

import { useMockableQuery } from "./helpers";
import { MOCK_IGNORED_FILES } from "./mocks/ignored-files.mock";

export const useIgnoredFilesQuery = (): UseQueryResult<
  ApplicationIgnoredFilesDto[],
  AxiosError
> => {
  const mapCallback = useCallback(
    (data: ApplicationIgnoredFilesDto[]): ApplicationIgnoredFilesDto[] => {
      return data.map((app) => ({
        ...app,
        ignoredFiles: app.ignoredFiles.sort((a, b) =>
          a.fileName.localeCompare(b.fileName)
        ),
      }));
    },
    []
  );

  return useMockableQuery<
    ApplicationIgnoredFilesDto[],
    AxiosError,
    ApplicationIgnoredFilesDto[]
  >(
    {
      queryKey: ["ignored-files"],
      queryFn: async () => {
        const url = "/ignored-files";
        return (await axios.get<ApplicationIgnoredFilesDto[]>(url)).data;
      },
      select: mapCallback,
    },
    MOCK_IGNORED_FILES,
    (window as any)["ignored-files"]
  );
};
