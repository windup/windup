import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationHibernateDto } from "@app/api/hibernate";

import { useMockableQuery } from "./helpers";
import { MOCK_HIBERNATE } from "./mocks/hibernate.mock";

export const useHibernateQuery = (): UseQueryResult<
  ApplicationHibernateDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationHibernateDto[], AxiosError>(
    {
      queryKey: ["hibernate"],
      queryFn: async () =>
        (await axios.get<ApplicationHibernateDto[]>("/hibernate")).data,
    },
    MOCK_HIBERNATE,
    (window as any)["hibernate"]
  );
};
