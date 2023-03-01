import { ApplicationSpringBeansDto } from "@app/api/spring-beans";

export let MOCK_SPRING_BEANS: ApplicationSpringBeansDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationSpringBeansDto = {
    applicationId: "app-1",
    beans: [],
  };

  const application2Beans: ApplicationSpringBeansDto = {
    applicationId: "app-2",
    beans: [],
  };

  MOCK_SPRING_BEANS = [application1Beans, application2Beans];
}
