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
import { useHibernateQuery } from "@app/queries/hibernate";

import { EntitiesTable } from "./entities-table";
import { SessionFactoriesTable } from "./session-factories-table";

export const Hibernate: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  const allHibernateQuery = useHibernateQuery();

  const applicationHibernate = useMemo(() => {
    return allHibernateQuery.data?.find(
      (f) => f.applicationId === application?.id
    );
  }, [allHibernateQuery.data, application]);

  return (
    <PageSection>
      <Card>
        <CardBody>
          <Tabs defaultActiveKey={0}>
            <Tab
              eventKey={0}
              title={
                <TabTitleText>
                  Session factory (
                  {
                    applicationHibernate?.hibernateConfigurations.flatMap(
                      (f) => f.sessionFactories
                    ).length
                  }
                  )
                </TabTitleText>
              }
            >
              {application && (
                <SessionFactoriesTable applicationId={application?.id} />
              )}
            </Tab>
            <Tab
              eventKey={1}
              title={
                <TabTitleText>
                  Entities ({applicationHibernate?.entities.length})
                </TabTitleText>
              }
            >
              {application && <EntitiesTable applicationId={application?.id} />}
            </Tab>
          </Tabs>
        </CardBody>
      </Card>
    </PageSection>
  );
};
