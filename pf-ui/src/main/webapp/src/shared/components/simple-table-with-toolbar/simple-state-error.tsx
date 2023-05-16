import React from 'react';

import {
  EmptyState,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
  EmptyStateBody,
} from '@patternfly/react-core';
import ExclamationCircleIcon from "@patternfly/react-icons/dist/esm/icons/exclamation-circle-icon";
import { global_danger_color_200 as globalDangerColor200 } from '@patternfly/react-tokens';

export const SimpleStateError: React.FC = () => {
  return (
    <EmptyState variant={EmptyStateVariant.small}>
      <EmptyStateIcon icon={ExclamationCircleIcon} color={globalDangerColor200.value} />
      <Title headingLevel="h2" size="lg">
        Unable to connect
      </Title>
      <EmptyStateBody>
        There was an error retrieving data. Check your connection and try again.
      </EmptyStateBody>
    </EmptyState>
  );
};
