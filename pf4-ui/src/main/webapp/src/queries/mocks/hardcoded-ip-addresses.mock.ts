import { ApplicationHardcodedIpAddressesDto } from "@app/api/hardcoded-ip-addresses";

export let MOCK_HARDCODED_IP_ADDRESSES: ApplicationHardcodedIpAddressesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const application1Beans: ApplicationHardcodedIpAddressesDto = {
    applicationId: "app-1",
    files: [],
  };

  const application2Beans: ApplicationHardcodedIpAddressesDto = {
    applicationId: "app-2",
    files: [],
  };

  MOCK_HARDCODED_IP_ADDRESSES = [application1Beans, application2Beans];
}
