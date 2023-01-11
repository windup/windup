import { ApplicationEJBsDto } from "@app/api/application-ejb";

export let MOCK_EJB: ApplicationEJBsDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationEJBsDto = {
    applicationId: "app-1",
    beans: [
      {
        type: "MESSAGE_DRIVEN_BEAN",
        classFileId: "file1",
        beanDescriptorFileId: "file2",
        beanName: "beanName1",
        className: "className1",
      },
    ],
  };

  const application2Beans: ApplicationEJBsDto = {
    applicationId: "app-2",
    beans: [
      {
        type: "MESSAGE_DRIVEN_BEAN",
        classFileId: "file3",
        beanDescriptorFileId: "file4",
        beanName: "beanName2",
        className: "className1",
      },
    ],
  };

  MOCK_EJB = [application1Beans, application2Beans];
}
