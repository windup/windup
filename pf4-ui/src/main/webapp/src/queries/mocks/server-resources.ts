import { ApplicationServerResourcesDto } from "@app/api/server-resources";

export let MOCK_SERVER_RESOURCES: ApplicationServerResourcesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationServerResourcesDto = {
    applicationId: "app-1",
    datasources: [],
    jmsConnectionFactories: [],
    jmsDestinations: [],
    otherJndiEntries: [],
    threadPools: [],
  };

  const application2Beans: ApplicationServerResourcesDto = {
    applicationId: "app-2",
    datasources: [],
    jmsConnectionFactories: [],
    jmsDestinations: [],
    otherJndiEntries: [],
    threadPools: [],
  };

  MOCK_SERVER_RESOURCES = [application1Beans, application2Beans];
}
