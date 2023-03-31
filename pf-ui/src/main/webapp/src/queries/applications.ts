import { useCallback } from "react";

import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationDto } from "@app/api/application";

import { useMockableQuery } from "./helpers";
import { MOCK_APPLICATIONS } from "./mocks/applications.mock";

export const useApplicationsQuery = (): UseQueryResult<
  ApplicationDto[],
  AxiosError
> => {
  const sortListCallback = useCallback(
    (data: ApplicationDto[]): ApplicationDto[] => {
      return data.sort((a, b) => {
        if (a.isVirtual) return -1;
        if (b.isVirtual) return 1;

        return a.name.localeCompare(b.name);
      });
    },
    []
  );

  return useMockableQuery<ApplicationDto[], AxiosError>(
    {
      queryKey: ["applications"],
      queryFn: async () =>
        (await axios.get<ApplicationDto[]>("/applications")).data,
      select: sortListCallback,
    },
    MOCK_APPLICATIONS,
    (window as any)["applications"]
  );
};
