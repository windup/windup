import { ApplicationEJBsDto } from "@app/api/ejb";

export let MOCK_EJB: ApplicationEJBsDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationEJBsDto = {
    applicationId: "app-1",
    entityBeans: [],
    sessionBeans: [],
    messageDrivenBeans: [],
  };

  const application2Beans: ApplicationEJBsDto = {
    applicationId: "app-2",
    entityBeans: [],
    sessionBeans: [],
    messageDrivenBeans: [],
  };

  MOCK_EJB = [application1Beans, application2Beans];
}
