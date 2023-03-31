import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { AnalysisConfigurationDto } from "@app/api/analysis-configuration";

import { useMockableQuery } from "./helpers";
import { MOCK_ANALYSIS_CONFIGURATION } from "./mocks/analysis-configuration";

export const useAnalysisConfigurationQuery = (): UseQueryResult<
  AnalysisConfigurationDto,
  AxiosError
> => {
  return useMockableQuery<AnalysisConfigurationDto, AxiosError>(
    {
      queryKey: ["analysis-configuration"],
      queryFn: async () =>
        (await axios.get<AnalysisConfigurationDto>("/analysis-configuration"))
          .data,
    },
    MOCK_ANALYSIS_CONFIGURATION,
    (window as any)["analysis-configuration"]
  );
};
