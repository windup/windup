import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationDetailsDto } from "@app/api/application-details";

import { useMockableQuery } from "./helpers";
import { MOCK_APPLICATIONS_DETAILS } from "./mocks/application-details.mock";

export const useApplicationsDetailsQuery = (): UseQueryResult<
  ApplicationDetailsDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationDetailsDto[], AxiosError>(
    {
      queryKey: ["applications-details"],
      queryFn: async () =>
        (await axios.get<ApplicationDetailsDto[]>("/applications-details"))
          .data,
    },
    MOCK_APPLICATIONS_DETAILS,
    (window as any)["applications-details"]
  );
};
