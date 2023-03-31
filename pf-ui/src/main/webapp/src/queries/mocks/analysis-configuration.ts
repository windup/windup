import { AnalysisConfigurationDto } from "@app/api/analysis-configuration";

export let MOCK_ANALYSIS_CONFIGURATION: AnalysisConfigurationDto;

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const configuration: AnalysisConfigurationDto = {
    exportCSV: false,
  };

  MOCK_ANALYSIS_CONFIGURATION = configuration;
}
