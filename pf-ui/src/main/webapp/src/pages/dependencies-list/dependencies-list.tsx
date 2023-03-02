import React from "react";
import { useMatch, useNavigate } from "react-router-dom";

import {
  Divider,
  PageSection,
  PageSectionVariants,
  Text,
  TextContent,
  Toolbar,
  ToolbarContent,
  ToolbarItem,
} from "@patternfly/react-core";

import { ALL_APPLICATIONS_ID } from "@app/Constants";
import { Context, SimpleContextSelector } from "@app/context/simple-context";
import { DependenciesTable } from "@app/shared/components";

export const DependenciesList: React.FC = () => {
  const matchDependenciesPage = useMatch("/dependencies");
  const matchAllDependenciesPage = useMatch("/dependencies/applications");
  const matchSingleApplicationPage = useMatch(
    "/dependencies/applications/:applicationId"
  );

  const applicationId = matchDependenciesPage
    ? undefined
    : matchAllDependenciesPage
    ? ALL_APPLICATIONS_ID
    : matchSingleApplicationPage?.params.applicationId;

  const navigate = useNavigate();

  const onContextChange = (context: Context) => {
    navigate("/dependencies/applications/" + context.key);
  };

  return (
    <>
      <PageSection padding={{ default: "noPadding" }}>
        <Toolbar>
          <ToolbarContent>
            <ToolbarItem>Application:</ToolbarItem>
            <ToolbarItem>
              <SimpleContextSelector
                contextKeyFromURL={applicationId}
                onChange={onContextChange}
              />
            </ToolbarItem>
          </ToolbarContent>
        </Toolbar>
      </PageSection>
      <Divider />
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">Dependencies</Text>
          <Text component="small">
            This report lists all found Java libraries embedded within the
            analyzed application.
          </Text>
        </TextContent>
      </PageSection>
      <PageSection variant={PageSectionVariants.default}>
        <DependenciesTable applicationId={applicationId} />
      </PageSection>
    </>
  );
};
