import React, { useMemo } from "react";
import { useOutletContext } from "react-router-dom";

import {
  Card,
  CardBody,
  CardTitle,
  Grid,
  GridItem,
  PageSection,
} from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";
import { useApplicationsDetailsQuery } from "@app/queries/applications-details";
import { JavaIncidentsByPackage } from "@app/shared/components";

import { ApplicationFilesTable } from "./components/application-files-table";
import { MessagesCard } from "./components/messages-card";
import { TagsChart } from "./components/tags-chart";

export const Details: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  const applicationsDetailsQuery = useApplicationsDetailsQuery();
  const applicationFiles = useMemo(() => {
    return (
      applicationsDetailsQuery.data?.find(
        (f) => f.applicationId === application?.id
      )?.applicationFiles || []
    );
  }, [applicationsDetailsQuery.data, application]);

  return (
    <>
      <PageSection>
        <Grid hasGutter md={6}>
          <GridItem>
            <Card isFullHeight>
              <CardTitle>Tags found - Occurrence found</CardTitle>
              <CardBody>
                <TagsChart applicationFile={applicationFiles} />
              </CardBody>
            </Card>
          </GridItem>
          <GridItem>
            <Card isFullHeight>
              <CardTitle>Messages</CardTitle>
              <CardBody>
                {application && <MessagesCard application={application} />}
              </CardBody>
            </Card>
          </GridItem>
        </Grid>
      </PageSection>
      <PageSection>
        {application && <JavaIncidentsByPackage application={application} />}
      </PageSection>
      <PageSection>
        {application && <ApplicationFilesTable application={application} />}
      </PageSection>
    </>
  );
};
