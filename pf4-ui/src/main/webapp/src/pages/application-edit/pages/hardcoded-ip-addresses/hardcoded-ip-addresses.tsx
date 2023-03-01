import React from "react";
import { useOutletContext } from "react-router-dom";

import { PageSection } from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";

import { HardcodedIpAddressesTable } from "./hard-coded-ip-addresses-table";

export const HardcodedIpAddresses: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  return (
    <>
      <PageSection>
        {application?.id && (
          <HardcodedIpAddressesTable applicationId={application?.id} />
        )}
      </PageSection>
    </>
  );
};
