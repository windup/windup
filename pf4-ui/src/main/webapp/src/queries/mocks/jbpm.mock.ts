import { ApplicationJBPMsDto } from "@app/api/jbpm";

export let MOCK_JBPM: ApplicationJBPMsDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationJBPMsDto = {
    applicationId: "app-1",
    jbpms: [],
  };

  const application2Beans: ApplicationJBPMsDto = {
    applicationId: "app-2",
    jbpms: [],
  };

  MOCK_JBPM = [application1Beans, application2Beans];
}
