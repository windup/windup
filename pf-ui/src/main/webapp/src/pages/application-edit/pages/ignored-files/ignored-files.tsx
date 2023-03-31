import React from "react";
import { useOutletContext } from "react-router-dom";

import { PageSection } from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";
import { IgnoredFilesTable } from "@app/shared/components";

export const IgnoredFiles: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  return (
    <PageSection>
      <IgnoredFilesTable applicationId={application?.id} />
    </PageSection>
  );
};
