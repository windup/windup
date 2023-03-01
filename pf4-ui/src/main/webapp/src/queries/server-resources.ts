import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationServerResourcesDto } from "@app/api/server-resources";

import { useMockableQuery } from "./helpers";
import { MOCK_SERVER_RESOURCES } from "./mocks/server-resources";

export const useServerResourcesQuery = (): UseQueryResult<
  ApplicationServerResourcesDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationServerResourcesDto[], AxiosError>(
    {
      queryKey: ["server-resources"],
      queryFn: async () =>
        (await axios.get<ApplicationServerResourcesDto[]>("/server-resources"))
          .data,
    },
    MOCK_SERVER_RESOURCES,
    (window as any)["server-resources"]
  );
};
