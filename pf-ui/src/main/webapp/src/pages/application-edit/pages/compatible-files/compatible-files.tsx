import React from "react";
import { useOutletContext } from "react-router-dom";

import { Alert, PageSection, Stack, StackItem } from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";

import { CompatibleFilesTable } from "./compatible-files-table";

export const CompatibleFiles: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  return (
    <>
      <PageSection>
        <Stack hasGutter>
          <StackItem>
            <Alert variant="info" title="Disclaimer">
              Files in this report are believed to be compatible with the
              selected target platform; however, it is possible that this report
              contains incompatible files that were not identified by any rules
              in the system. It is recommended that these files be reviewed
              manually for any issues.
            </Alert>
          </StackItem>
          <StackItem>
            {application?.id && (
              <CompatibleFilesTable applicationId={application?.id} />
            )}
          </StackItem>
        </Stack>
      </PageSection>
    </>
  );
};
