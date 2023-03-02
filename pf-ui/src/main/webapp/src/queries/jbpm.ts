import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationJBPMsDto } from "@app/api/jbpm";

import { useMockableQuery } from "./helpers";
import { MOCK_JBPM } from "./mocks/jbpm.mock";

export const useJBPMsQuery = (): UseQueryResult<
  ApplicationJBPMsDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationJBPMsDto[], AxiosError>(
    {
      queryKey: ["jbpm"],
      queryFn: async () =>
        (await axios.get<ApplicationJBPMsDto[]>("/jbpm")).data,
    },
    MOCK_JBPM,
    (window as any)["jbpm"]
  );
};
