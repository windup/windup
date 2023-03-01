import { ApplicationJPAsDto } from "@app/api/jpa";

export let MOCK_JPA: ApplicationJPAsDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationJPAsDto = {
    applicationId: "app-1",
    entities: [],
    jpaConfigurations: [],
    namesQueries: [],
  };

  const application2Beans: ApplicationJPAsDto = {
    applicationId: "app-2",
    entities: [],
    jpaConfigurations: [],
    namesQueries: [],
  };

  MOCK_JPA = [application1Beans, application2Beans];
}
