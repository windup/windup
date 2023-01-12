import React, { useMemo } from "react";
import { useOutletContext } from "react-router-dom";

import {
  Bullseye,
  Card,
  CardBody,
  CardTitle,
  EmptyState,
  EmptyStateIcon,
  PageSection,
  Title,
} from "@patternfly/react-core";
import { InfoAltIcon } from "@patternfly/react-icons";

import { ApplicationDto } from "@app/api/application";
import { useEJBsQuery } from "@app/queries/ejb";

import { EntityBeanTable } from "./entity-beans-table";
import { MessageDrivenBeansTable } from "./message-driven-beans-table";
import { StatelessSessionBeansTable } from "./stateless-session-beans-table";

export const ApplicationEJBs: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  const allEJBsQuery = useEJBsQuery();

  const messageDrivenBeans = useMemo(() => {
    return allEJBsQuery.data?.find((f) => f.applicationId === application?.id)
      ?.messageDrivenBeans;
  }, [allEJBsQuery.data, application]);

  const statelessSessionBeans = useMemo(() => {
    return allEJBsQuery.data
      ?.find((f) => f.applicationId === application?.id)
      ?.sessionBeans.filter((f) => f.type === "STATELESS");
  }, [allEJBsQuery.data, application]);

  const statefulSessionBeans = useMemo(() => {
    return allEJBsQuery.data
      ?.find((f) => f.applicationId === application?.id)
      ?.sessionBeans.filter((f) => f.type === "STATEFUL");
  }, [allEJBsQuery.data, application]);

  const entityBeans = useMemo(() => {
    return allEJBsQuery.data?.find((f) => f.applicationId === application?.id)
      ?.entityBeans;
  }, [allEJBsQuery.data, application]);

  return (
    <>
      {(messageDrivenBeans?.length ?? 0) +
        (statelessSessionBeans?.length ?? 0) +
        (statefulSessionBeans?.length ?? 0) +
        (entityBeans?.length ?? 0) ===
        0 && (
        <PageSection>
          <Card>
            <CardBody>
              <Bullseye>
                <EmptyState>
                  <EmptyStateIcon icon={InfoAltIcon} />
                  <Title headingLevel="h4" size="lg">
                    No EJBs found
                  </Title>
                </EmptyState>
              </Bullseye>
            </CardBody>
          </Card>
        </PageSection>
      )}
      {messageDrivenBeans && messageDrivenBeans.length > 0 && (
        <PageSection>
          <Card>
            <CardTitle>Message driven beans</CardTitle>
            <CardBody>
              {application?.id && (
                <MessageDrivenBeansTable applicationId={application?.id} />
              )}
            </CardBody>
          </Card>
        </PageSection>
      )}
      {statelessSessionBeans && statelessSessionBeans.length > 0 && (
        <PageSection>
          <Card>
            <CardTitle>Stateless sessions beans</CardTitle>
            <CardBody>
              {application?.id && (
                <StatelessSessionBeansTable
                  applicationId={application?.id}
                  sessionBeanType="STATELESS"
                />
              )}
            </CardBody>
          </Card>
        </PageSection>
      )}
      {statefulSessionBeans && statefulSessionBeans.length > 0 && (
        <PageSection>
          <Card>
            <CardTitle>Stateful sessions beans</CardTitle>
            <CardBody>
              {application?.id && (
                <StatelessSessionBeansTable
                  applicationId={application?.id}
                  sessionBeanType="STATEFUL"
                />
              )}
            </CardBody>
          </Card>
        </PageSection>
      )}
      {entityBeans && entityBeans.length > 0 && (
        <PageSection>
          <Card>
            <CardTitle>Entity beans</CardTitle>
            <CardBody>
              {application?.id && (
                <EntityBeanTable applicationId={application?.id} />
              )}
            </CardBody>
          </Card>
        </PageSection>
      )}
    </>
  );
};
