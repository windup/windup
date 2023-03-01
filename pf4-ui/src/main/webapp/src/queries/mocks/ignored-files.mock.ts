import { ApplicationIgnoredFilesDto } from "@app/api/ignored-files";

export let MOCK_IGNORED_FILES: ApplicationIgnoredFilesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Deps: ApplicationIgnoredFilesDto = {
    applicationId: "app-1",
    ignoredFiles: [
      {
        fileName: "randomfile1",
        filePath: "randomfile1Path",
        reason: "randomfile1Reason",
      },
    ],
  };

  const application2Deps: ApplicationIgnoredFilesDto = {
    applicationId: "app-2",
    ignoredFiles: [
      {
        fileName: "randomfile2",
        filePath: "randomfile2Path",
        reason: "randomfile2Reason",
      },
    ],
  };

  MOCK_IGNORED_FILES = [application1Deps, application2Deps];
}
