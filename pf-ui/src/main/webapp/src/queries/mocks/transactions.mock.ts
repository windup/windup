import { ApplicationTransactionsDto } from "@app/api/transactions";

export let MOCK_TRANSACTIONS: ApplicationTransactionsDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationTransactionsDto = {
    applicationId: "app-1",
    transactions: [],
  };

  const application2Beans: ApplicationTransactionsDto = {
    applicationId: "app-2",
    transactions: [],
  };

  MOCK_TRANSACTIONS = [application1Beans, application2Beans];
}
