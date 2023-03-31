import React from "react";
import { useOutletContext } from "react-router-dom";

import { PageSection } from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";

import { JBPMTable } from "./jbpm-table";

export const JBPM: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  return (
    <PageSection>
      <JBPMTable applicationId={application?.id} />
    </PageSection>
  );
};
