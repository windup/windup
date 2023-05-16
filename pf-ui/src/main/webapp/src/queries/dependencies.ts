import { useCallback } from "react";

import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationDependenciesDto } from "@app/api/dependencies";

import { useMockableQuery } from "./helpers";
import { MOCK_DEPENDENCIES } from "./mocks/dependencies.mock";

export const useDependenciesQuery = (): UseQueryResult<
  ApplicationDependenciesDto[],
  AxiosError
> => {
  const mapCallback = useCallback(
    (data: ApplicationDependenciesDto[]): ApplicationDependenciesDto[] => {
      return data.map((app) => ({
        ...app,
        dependencies: app.dependencies.sort((a, b) =>
          a.name.localeCompare(b.name)
        ),
      }));
    },
    []
  );

  return useMockableQuery<
    ApplicationDependenciesDto[],
    AxiosError,
    ApplicationDependenciesDto[]
  >(
    {
      queryKey: ["dependencies"],
      queryFn: async () => {
        const url = "/dependencies";
        return (await axios.get<ApplicationDependenciesDto[]>(url)).data;
      },
      select: mapCallback,
    },
    MOCK_DEPENDENCIES,
    (window as any)["dependencies"]
  );
};
