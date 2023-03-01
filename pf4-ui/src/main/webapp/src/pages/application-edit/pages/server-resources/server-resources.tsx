import React, { useMemo } from "react";
import { useOutletContext } from "react-router-dom";

import {
  Card,
  CardBody,
  PageSection,
  Tab,
  Tabs,
  TabTitleText,
} from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";
import { useServerResourcesQuery } from "@app/queries/server-resources";

import { DatasourcesTable } from "./datasource-table";
import { JMSConnectionFactoryTable } from "./jms-connection-factory-table";
import { JMSDestinationsTable } from "./jms-destinations-table";
import { OtherJndiEntriesTable } from "./other-jndi-entries-table";
import { ThreadPoolsTable } from "./thread-pools-table";

export const ServerResources: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  const allServerResourcesQuery = useServerResourcesQuery();

  const applicationServerResources = useMemo(() => {
    return allServerResourcesQuery.data?.find(
      (f) => f.applicationId === application?.id
    );
  }, [allServerResourcesQuery.data, application]);

  return (
    <PageSection>
      <Card>
        <CardBody>
          <Tabs defaultActiveKey={0}>
            <Tab
              eventKey={0}
              title={
                <TabTitleText>
                  Datasources ({applicationServerResources?.datasources.length})
                </TabTitleText>
              }
            >
              {application && (
                <DatasourcesTable applicationId={application?.id} />
              )}
            </Tab>
            <Tab
              eventKey={1}
              title={
                <TabTitleText>
                  JMS destinations (
                  {applicationServerResources?.jmsDestinations.length})
                </TabTitleText>
              }
            >
              {application && (
                <JMSDestinationsTable applicationId={application?.id} />
              )}
            </Tab>
            <Tab
              eventKey={2}
              title={
                <TabTitleText>
                  JMS connection factories (
                  {applicationServerResources?.jmsConnectionFactories.length})
                </TabTitleText>
              }
            >
              {application && (
                <JMSConnectionFactoryTable applicationId={application?.id} />
              )}
            </Tab>
            <Tab
              eventKey={3}
              title={
                <TabTitleText>
                  Thread pools ({applicationServerResources?.threadPools.length}
                  )
                </TabTitleText>
              }
            >
              {application && (
                <ThreadPoolsTable applicationId={application?.id} />
              )}
            </Tab>
            <Tab
              eventKey={4}
              title={
                <TabTitleText>
                  Other JNDI entries (
                  {applicationServerResources?.otherJndiEntries.length})
                </TabTitleText>
              }
            >
              {application && (
                <OtherJndiEntriesTable applicationId={application?.id} />
              )}
            </Tab>
          </Tabs>
        </CardBody>
      </Card>
    </PageSection>
  );
};
