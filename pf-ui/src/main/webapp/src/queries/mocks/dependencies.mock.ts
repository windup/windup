import { ApplicationDependenciesDto } from "@app/api/dependencies";

export let MOCK_DEPENDENCIES: ApplicationDependenciesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Deps: ApplicationDependenciesDto = {
    applicationId: "app-1",
    dependencies: [
      {
        name: "dep1.jar",
        sha1: "1234",
        version: "1.0",
        foundPaths: ["path1"],
      },
    ],
  };

  const application2Deps: ApplicationDependenciesDto = {
    applicationId: "app-2",
    dependencies: [
      {
        name: "dep2.jar",
        sha1: "4321",
        version: "2.0",
        foundPaths: ["path2"],
      },
    ],
  };

  MOCK_DEPENDENCIES = [application1Deps, application2Deps];
}
