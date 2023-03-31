import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationSpringBeansDto } from "@app/api/spring-beans";

import { useMockableQuery } from "./helpers";
import { MOCK_SPRING_BEANS } from "./mocks/spring-beans.mock";

export const useSpringBeansQuery = (): UseQueryResult<
  ApplicationSpringBeansDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationSpringBeansDto[], AxiosError>(
    {
      queryKey: ["spring-beans"],
      queryFn: async () =>
        (await axios.get<ApplicationSpringBeansDto[]>("/spring-beans")).data,
    },
    MOCK_SPRING_BEANS,
    (window as any)["spring-beans"]
  );
};
