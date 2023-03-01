import { ApplicationUnparsableFilesDto } from "@app/api/unparsable-files";

export let MOCK_UNPARSABLE_FILES: ApplicationUnparsableFilesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationUnparsableFilesDto = {
    applicationId: "app-1",
    subProjects: [],
  };

  const application2Beans: ApplicationUnparsableFilesDto = {
    applicationId: "app-2",
    subProjects: [],
  };

  MOCK_UNPARSABLE_FILES = [application1Beans, application2Beans];
}
