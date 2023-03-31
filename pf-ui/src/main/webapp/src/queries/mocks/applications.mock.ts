import { ApplicationDto } from "@app/api/application";

export let MOCK_APPLICATIONS: ApplicationDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1: ApplicationDto = {
    id: "app-1",
    name: "app1.jar",
    isVirtual: false,
    tags: ["tag1", "tag2"],
    storyPoints: 45,
    incidents: {
      mandatory: 7,
      optional: 27,
      potential: 5,
      information: 68,
      "cloud-mandatory": 0,
      "cloud-optional": 0,
    },
  };

  const application2: ApplicationDto = {
    id: "app-2",
    name: "app2.jar",
    isVirtual: true,
    tags: ["tag1", "tag2"],
    storyPoints: 90,
    incidents: {
      mandatory: 53,
      optional: 1,
      potential: 38,
      information: 11,
      "cloud-mandatory": 0,
      "cloud-optional": 0,
    },
  };

  MOCK_APPLICATIONS = [application1, application2];
}
