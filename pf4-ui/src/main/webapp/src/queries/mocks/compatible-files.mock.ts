import { ApplicationCompatibleFilesDto } from "@app/api/compatible-files";

export let MOCK_COMPATIBLE_FILES: ApplicationCompatibleFilesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationCompatibleFilesDto = {
    applicationId: "app-1",
    artifacts: [],
  };

  const application2Beans: ApplicationCompatibleFilesDto = {
    applicationId: "app-2",
    artifacts: [],
  };

  MOCK_COMPATIBLE_FILES = [application1Beans, application2Beans];
}
