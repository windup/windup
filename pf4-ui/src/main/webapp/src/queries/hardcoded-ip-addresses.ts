import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { ApplicationHardcodedIpAddressesDto } from "@app/api/hardcoded-ip-addresses";

import { useMockableQuery } from "./helpers";
import { MOCK_HARDCODED_IP_ADDRESSES } from "./mocks/hardcoded-ip-addresses.mock";

export const useHardcodedIpAddressesQuery = (): UseQueryResult<
  ApplicationHardcodedIpAddressesDto[],
  AxiosError
> => {
  return useMockableQuery<ApplicationHardcodedIpAddressesDto[], AxiosError>(
    {
      queryKey: ["hardcoded-ip-addresses"],
      queryFn: async () =>
        (
          await axios.get<ApplicationHardcodedIpAddressesDto[]>(
            "/hardcoded-ip-addresses"
          )
        ).data,
    },
    MOCK_HARDCODED_IP_ADDRESSES,
    (window as any)["hardcoded-ip-addresses"]
  );
};
