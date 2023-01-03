import React from "react";

import { Page } from "@patternfly/react-core";

import { HeaderApp } from "./header";
import { SidebarApp } from "./sidebar";

export const DefaultLayout: React.FC = ({ children }) => {
  return (
    <Page header={<HeaderApp />} sidebar={<SidebarApp />} isManagedSidebar>
      {children}
    </Page>
  );
};
