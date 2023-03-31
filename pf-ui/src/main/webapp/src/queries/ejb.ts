import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationEJBsDto } from "@app/api/ejb";

import { useMockableQuery } from "./helpers";
import { MOCK_EJB } from "./mocks/ejb.mock";

export const useEJBsQuery = (): UseQueryResult<
  ApplicationEJBsDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationEJBsDto[], AxiosError>(
    {
      queryKey: ["ejb"],
      queryFn: async () =>
        (await axios.get<ApplicationEJBsDto[]>("/ejb")).data,
    },
    MOCK_EJB,
    (window as any)["ejb"]
  );
};
