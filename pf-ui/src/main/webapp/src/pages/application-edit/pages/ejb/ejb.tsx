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
import { useEJBsQuery } from "@app/queries/ejb";

import { EntityBeanTable } from "./entity-beans-table";
import { MessageDrivenBeansTable } from "./message-driven-beans-table";
import { StatelessSessionBeansTable } from "./stateless-session-beans-table";

export const EJB: React.FC = () => {
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
      <PageSection>
        <Card>
          <CardBody>
            <Tabs defaultActiveKey={0}>
              <Tab
                eventKey={0}
                title={
                  <TabTitleText>
                    Message driven beans ({messageDrivenBeans?.length})
                  </TabTitleText>
                }
              >
                {application?.id && (
                  <MessageDrivenBeansTable applicationId={application?.id} />
                )}
              </Tab>
              <Tab
                eventKey={1}
                title={
                  <TabTitleText>
                    Stateless sessions beans ({statelessSessionBeans?.length})
                  </TabTitleText>
                }
              >
                {application?.id && (
                  <StatelessSessionBeansTable
                    applicationId={application?.id}
                    sessionBeanType="STATELESS"
                  />
                )}
              </Tab>
              <Tab
                eventKey={2}
                title={
                  <TabTitleText>
                    Stateful sessions beans ({statefulSessionBeans?.length})
                  </TabTitleText>
                }
              >
                {application?.id && (
                  <StatelessSessionBeansTable
                    applicationId={application?.id}
                    sessionBeanType="STATEFUL"
                  />
                )}
              </Tab>
              <Tab
                eventKey={3}
                title={
                  <TabTitleText>
                    Entity beans({entityBeans?.length})
                  </TabTitleText>
                }
              >
                {application?.id && (
                  <EntityBeanTable applicationId={application?.id} />
                )}
              </Tab>
            </Tabs>
          </CardBody>
        </Card>
      </PageSection>
    </>
  );
};
