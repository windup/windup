import { ApplicationRemoteServicesDto } from "@app/api/remote-services";

export let MOCK_REMOTE_SERVICES: ApplicationRemoteServicesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationRemoteServicesDto = {
    applicationId: "app-1",
    jaxRsServices: [],
    jaxWsServices: [],
    ejbRemoteServices: [],
    rmiServices: [],
  };

  const application2Beans: ApplicationRemoteServicesDto = {
    applicationId: "app-2",
    jaxRsServices: [],
    jaxWsServices: [],
    ejbRemoteServices: [],
    rmiServices: [],
  };

  MOCK_REMOTE_SERVICES = [application1Beans, application2Beans];
}
