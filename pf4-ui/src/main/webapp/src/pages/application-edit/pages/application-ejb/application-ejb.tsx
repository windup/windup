import React from "react";
import { useOutletContext } from "react-router-dom";

import { Card, CardBody, CardTitle, PageSection } from "@patternfly/react-core";

import { ApplicationDto } from "@app/api/application";
import { MessageDrivenBeansTable } from "./message-driven-beans-table";
import { StatelessSessionBeansTable } from "./stateless-session-beans-table";

export const ApplicationEJBs: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  return (
    <>
      <PageSection>
        <Card>
          <CardTitle>Message driven beans</CardTitle>
          <CardBody>
            <MessageDrivenBeansTable applicationId={application?.id} />
          </CardBody>
        </Card>
      </PageSection>
      <PageSection>
        <Card>
          <CardTitle>Stateless sessions beans</CardTitle>
          <CardBody>
            <StatelessSessionBeansTable
              applicationId={application?.id}
              sessionBeanType="STATELESS_SESSION_BEAN"
            />
          </CardBody>
        </Card>
      </PageSection>
      <PageSection>
        <Card>
          <CardTitle>Stateful sessions beans</CardTitle>
          <CardBody>
            <StatelessSessionBeansTable
              applicationId={application?.id}
              sessionBeanType="STATEFUL_SESSION_BEAN"
            />
          </CardBody>
        </Card>
      </PageSection>
    </>
  );
};
