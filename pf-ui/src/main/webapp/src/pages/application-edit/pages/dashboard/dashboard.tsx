import React from "react";
import { useOutletContext } from "react-router-dom";

import { PageSection, Stack, StackItem } from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";
import { JavaIncidentsByPackage } from "@app/shared/components";

import { EffortsSection } from "./components/efforts-section";
import { IncidentsSection } from "./components/incidents-section";

export const Dashboard: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  return (
    <>
      <PageSection>
        <Stack hasGutter>
          <StackItem>
            {application && <IncidentsSection application={application} />}
          </StackItem>
          <StackItem>
            {application && <EffortsSection application={application} />}
          </StackItem>
          <StackItem>
            {application && (
              <JavaIncidentsByPackage application={application} />
            )}
          </StackItem>
        </Stack>
      </PageSection>
    </>
  );
};
