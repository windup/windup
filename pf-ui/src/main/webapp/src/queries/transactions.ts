import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationTransactionsDto } from "@app/api/transactions";

import { useMockableQuery } from "./helpers";
import { MOCK_TRANSACTIONS } from "./mocks/transactions.mock";

export const useTransactionsQuery = (): UseQueryResult<
  ApplicationTransactionsDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationTransactionsDto[], AxiosError>(
    {
      queryKey: ["transactions"],
      queryFn: async () =>
        (await axios.get<ApplicationTransactionsDto[]>("/transactions")).data,
    },
    MOCK_TRANSACTIONS,
    (window as any)["transactions"]
  );
};
