import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationJPAsDto } from "@app/api/jpa";

import { useMockableQuery } from "./helpers";
import { MOCK_JPA } from "./mocks/jpa.mock";

export const useJPAsQuery = (): UseQueryResult<
  ApplicationJPAsDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationJPAsDto[], AxiosError>(
    {
      queryKey: ["jpa"],
      queryFn: async () => (await axios.get<ApplicationJPAsDto[]>("/jpa")).data,
    },
    MOCK_JPA,
    (window as any)["jpa"]
  );
};
