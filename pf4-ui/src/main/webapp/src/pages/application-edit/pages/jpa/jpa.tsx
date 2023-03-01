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
import { useJPAsQuery } from "@app/queries/jpa";

import { EntitiesTable } from "./entities-table";
import { NamedQueriesTable } from "./named-queries-table";
import { PersistenceUnitTable } from "./persistence-units-table";

export const JPA: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  const allJPAsQuery = useJPAsQuery();

  const applicationJPAs = useMemo(() => {
    return allJPAsQuery.data?.find((f) => f.applicationId === application?.id);
  }, [allJPAsQuery.data, application]);

  return (
    <PageSection>
      <Card>
        <CardBody>
          <Tabs defaultActiveKey={0}>
            <Tab
              eventKey={0}
              title={
                <TabTitleText>
                  Persistence units (
                  {
                    applicationJPAs?.jpaConfigurations.flatMap(
                      (f) => f.persistentUnits
                    ).length
                  }
                  )
                </TabTitleText>
              }
            >
              {application && (
                <PersistenceUnitTable applicationId={application?.id} />
              )}
            </Tab>
            <Tab
              eventKey={1}
              title={
                <TabTitleText>
                  Entities ({applicationJPAs?.entities.length})
                </TabTitleText>
              }
            >
              {application && <EntitiesTable applicationId={application?.id} />}
            </Tab>
            <Tab
              eventKey={2}
              title={
                <TabTitleText>
                  Named queries ({applicationJPAs?.namesQueries.length})
                </TabTitleText>
              }
            >
              {application && (
                <NamedQueriesTable applicationId={application?.id} />
              )}
            </Tab>
          </Tabs>
        </CardBody>
      </Card>
    </PageSection>
  );
};
