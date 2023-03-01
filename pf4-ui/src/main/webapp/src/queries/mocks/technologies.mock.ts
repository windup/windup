import { ApplicationTechnologiesDto } from "@app/api/technologies";

export let MOCK_TECHNOLOGIES: ApplicationTechnologiesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Deps: ApplicationTechnologiesDto = {
    applicationId: "app-1",
    technologyGroups: {
      View: {
        Web: {
          total: 2,
          "Web XML File": 1,
          "Weblogic Web XML": 1,
        },
      },
      Connect: {},
      Store: {},
      Sustain: {},
      Execute: {},
    },
  };

  const application2Deps: ApplicationTechnologiesDto = {
    applicationId: "app-2",
    technologyGroups: {
      View: {},
      Connect: {
        Streaming: {
          total: 0,
        },
      },
      Store: {},
      Sustain: {},
      Execute: {},
    },
  };

  MOCK_TECHNOLOGIES = [application1Deps, application2Deps];
}
