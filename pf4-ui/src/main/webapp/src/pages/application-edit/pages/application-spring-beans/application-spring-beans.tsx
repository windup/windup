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
import { useSpringBeansQuery } from "@app/queries/spring-beans";

import { SpringBeansTable } from "./spring-beans-table";

export const ApplicationSpringBeans: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  const allSpringBeansQuery = useSpringBeansQuery();

  const beans = useMemo(() => {
    return (
      allSpringBeansQuery.data?.find((f) => f.applicationId === application?.id)
        ?.beans || []
    );
  }, [allSpringBeansQuery.data, application]);

  return (
    <>
      {(beans?.length ?? 0) === 0 && (
        <PageSection>
          <Card>
            <CardBody>
              <Bullseye>
                <EmptyState>
                  <EmptyStateIcon icon={InfoAltIcon} />
                  <Title headingLevel="h4" size="lg">
                    No Spring beans found
                  </Title>
                </EmptyState>
              </Bullseye>
            </CardBody>
          </Card>
        </PageSection>
      )}
      {beans && beans.length > 0 && (
        <PageSection>
          <Card>
            <CardTitle>Spring beans</CardTitle>
            <CardBody>
              {application?.id && (
                <SpringBeansTable applicationId={application?.id} />
              )}
            </CardBody>
          </Card>
        </PageSection>
      )}
    </>
  );
};
