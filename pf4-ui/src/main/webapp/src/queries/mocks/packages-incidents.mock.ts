import { ApplicationPackageIncidentsDto } from "@app/api/package-incidents";

export let MOCK_PACKAGES: ApplicationPackageIncidentsDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Deps: ApplicationPackageIncidentsDto = {
    applicationId: "app-1",
    packages: {
      package1: 1,
      package2: 2,
    },
  };

  const application2Deps: ApplicationPackageIncidentsDto = {
    applicationId: "app-2",
    packages: {
      package1: 1,
      package2: 2,
    },
  };

  MOCK_PACKAGES = [application1Deps, application2Deps];
}
