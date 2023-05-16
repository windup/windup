import { ApplicationHibernateDto } from "@app/api/hibernate";

export let MOCK_HIBERNATE: ApplicationHibernateDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationHibernateDto = {
    applicationId: "app-1",
    entities: [],
    hibernateConfigurations: [],
  };

  const application2Beans: ApplicationHibernateDto = {
    applicationId: "app-2",
    entities: [],
    hibernateConfigurations: [],
  };

  MOCK_HIBERNATE = [application1Beans, application2Beans];
}
