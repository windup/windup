import { useMemo } from "react";
import {
  Link,
  Outlet,
  useLocation,
  useMatch,
  useNavigate,
} from "react-router-dom";

import {
  Breadcrumb,
  BreadcrumbItem,
  PageSection,
  Tab,
  TabTitleText,
  Tabs,
  Text,
  TextContent,
} from "@patternfly/react-core";

import { useApplicationsQuery } from "@app/queries/applications";
import { useTransactionsQuery } from "@app/queries/transactions";

export const ApplicationEdit: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const routeParams = useMatch("/applications/:applicationId/*");

  const applicationsQuery = useApplicationsQuery();
  const applicationsTransactions = useTransactionsQuery();

  const application = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    return (
      applicationsQuery.data?.find((app) => app.id === applicationId) || null
    );
  }, [routeParams?.params, applicationsQuery.data]);

  const applicationTransactions = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    return (
      applicationsTransactions.data?.find(
        (app) => app.applicationId === applicationId
      )?.transactions || null
    );
  }, [routeParams?.params, applicationsTransactions.data]);

  const tabItems: { title: string; path: string }[] = [
    {
      title: "Dashboard",
      path: `/applications/${application?.id}/dashboard`,
    },
    {
      title: "Issues",
      path: `/applications/${application?.id}/issues`,
    },
    {
      title: "Details",
      path: `/applications/${application?.id}/details`,
    },
    {
      title: "Technologies",
      path: `/applications/${application?.id}/technologies`,
    },
    {
      title: "Dependencies",
      path: `/applications/${application?.id}/dependencies`,
    },
    {
      title: "Ignored files",
      path: `/applications/${application?.id}/ignored-files`,
    },
    {
      title: "EJB",
      path: `/applications/${application?.id}/ejb`,
    },
    {
      title: "JPA",
      path: `/applications/${application?.id}/jpa`,
    },
    {
      title: "Hibernate",
      path: `/applications/${application?.id}/hibernate`,
    },
    {
      title: "Spring beans",
      path: `/applications/${application?.id}/spring-beans`,
    },
    {
      title: "Remote services",
      path: `/applications/${application?.id}/remote-services`,
    },
    {
      title: "Server resources",
      path: `/applications/${application?.id}/server-resources`,
    },
    {
      title: "Unparsable files",
      path: `/applications/${application?.id}/unparsable-files`,
    },
    {
      title: "Hard-coded IP addresses",
      path: `/applications/${application?.id}/hardcoded-ip-addresses`,
    },
    {
      title: "Compatible files",
      path: `/applications/${application?.id}/compatible-files`,
    },
    {
      title: "JBPM Processes",
      path: `/applications/${application?.id}/jbpm`,
    },
  ];

  if (applicationTransactions && applicationTransactions.length > 0) {
    tabItems.push({
      title: "Transactions",
      path: `/applications/${application?.id}/transactions`,
    });
  }

  return (
    <>
      <PageSection type="breadcrumb">
        <Breadcrumb>
          <BreadcrumbItem>
            <Link to="/applications">Applications</Link>
          </BreadcrumbItem>
          <BreadcrumbItem isActive>{application?.name}</BreadcrumbItem>
        </Breadcrumb>
      </PageSection>
      <PageSection type="default" variant="light">
        <TextContent>
          <Text component="h1">{application?.name}</Text>
        </TextContent>
      </PageSection>
      <PageSection type="tabs" variant="light">
        <Tabs
          role="region"
          activeKey={tabItems.find((e) => e.path === location.pathname)?.path}
          onSelect={(_, tabKey) => navigate(`${tabKey}`)}
          isOverflowHorizontal={{ showTabCount: true }}
          // isFilled={true}
        >
          {tabItems.map((e, index) => (
            <Tab
              key={index}
              eventKey={e.path}
              title={<TabTitleText>{e.title}</TabTitleText>}
            />
          ))}
        </Tabs>
      </PageSection>
      <Outlet context={application} />
    </>
  );
};