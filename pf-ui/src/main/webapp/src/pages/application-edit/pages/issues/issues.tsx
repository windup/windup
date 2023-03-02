import React from "react";
import { useOutletContext } from "react-router-dom";

import { PageSection } from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";
import { IssuesTable } from "@app/shared/components";

export const Issues: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  return (
    <PageSection>
      <IssuesTable applicationId={application?.id} />
    </PageSection>
  );
};
