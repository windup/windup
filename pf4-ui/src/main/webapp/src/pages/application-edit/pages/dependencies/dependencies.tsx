import React from "react";
import { useOutletContext } from "react-router-dom";

import { PageSection } from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";
import { DependenciesTable } from "@app/shared/components";

export const Dependencies: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  return (
    <PageSection>
      <DependenciesTable applicationId={application?.id} />
    </PageSection>
  );
};
