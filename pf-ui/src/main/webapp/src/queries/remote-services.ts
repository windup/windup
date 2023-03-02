import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationRemoteServicesDto } from "@app/api/remote-services";

import { useMockableQuery } from "./helpers";
import { MOCK_REMOTE_SERVICES } from "./mocks/remote-services.mock";

export const useRemoteServicesQuery = (): UseQueryResult<
  ApplicationRemoteServicesDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationRemoteServicesDto[], AxiosError>(
    {
      queryKey: ["remote-services"],
      queryFn: async () =>
        (await axios.get<ApplicationRemoteServicesDto[]>("/remote-services"))
          .data,
    },
    MOCK_REMOTE_SERVICES,
    (window as any)["remote-services"]
  );
};
