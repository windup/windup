import React from "react";

import {
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
} from "@patternfly/react-core";
import CubesIcon from "@patternfly/react-icons/dist/esm/icons/cubes-icon";

export const SimpleStateNoData: React.FC = () => {
  return (
    <EmptyState variant={EmptyStateVariant.small}>
      <EmptyStateIcon icon={CubesIcon} />
      <Title headingLevel="h2" size="lg">
        No data available
      </Title>
      <EmptyStateBody>No data available to be shown here.</EmptyStateBody>
    </EmptyState>
  );
};
