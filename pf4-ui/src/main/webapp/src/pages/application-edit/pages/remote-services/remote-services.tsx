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
import { useRemoteServicesQuery } from "@app/queries/remote-services";

import { EJBRemoteTable } from "./ejb-remote-table";
import { JaxRsTable } from "./jaxrs-table";
import { JaxWsTable } from "./jaxws-table";
import { RMITable } from "./rmi-table";

export const RemoteServices: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  const allRemoteServicesQuery = useRemoteServicesQuery();

  const applicationRemoteServices = useMemo(() => {
    return allRemoteServicesQuery.data?.find(
      (f) => f.applicationId === application?.id
    );
  }, [allRemoteServicesQuery.data, application]);

  return (
    <PageSection>
      <Card>
        <CardBody>
          <Tabs defaultActiveKey={0}>
            <Tab
              eventKey={0}
              title={
                <TabTitleText>
                  Jax-RS ({applicationRemoteServices?.jaxRsServices.length})
                </TabTitleText>
              }
            >
              {application && <JaxRsTable applicationId={application?.id} />}
            </Tab>
            <Tab
              eventKey={1}
              title={
                <TabTitleText>
                  Jax-WS ({applicationRemoteServices?.jaxWsServices.length})
                </TabTitleText>
              }
            >
              {application && <JaxWsTable applicationId={application?.id} />}
            </Tab>
            <Tab
              eventKey={2}
              title={
                <TabTitleText>
                  EJB remote (
                  {applicationRemoteServices?.ejbRemoteServices.length})
                </TabTitleText>
              }
            >
              {application && (
                <EJBRemoteTable applicationId={application?.id} />
              )}
            </Tab>
            <Tab
              eventKey={3}
              title={
                <TabTitleText>
                  RMI ({applicationRemoteServices?.rmiServices.length})
                </TabTitleText>
              }
            >
              {application && <RMITable applicationId={application?.id} />}
            </Tab>
          </Tabs>
        </CardBody>
      </Card>
    </PageSection>
  );
};
