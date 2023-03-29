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
import { useCompatibleFilesQuery } from "@app/queries/compatible-files";
import { useEJBsQuery } from "@app/queries/ejb";
import { useHardcodedIpAddressesQuery } from "@app/queries/hardcoded-ip-addresses";
import { useHibernateQuery } from "@app/queries/hibernate";
import { useIgnoredFilesQuery } from "@app/queries/ignored-files";
import { useJBPMsQuery } from "@app/queries/jbpm";
import { useJPAsQuery } from "@app/queries/jpa";
import { useRemoteServicesQuery } from "@app/queries/remote-services";
import { useServerResourcesQuery } from "@app/queries/server-resources";
import { useSpringBeansQuery } from "@app/queries/spring-beans";
import { useTransactionsQuery } from "@app/queries/transactions";
import { useUnparsableFilesQuery } from "@app/queries/unparsable-files";

export const ApplicationEdit: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const routeParams = useMatch("/applications/:applicationId/*");

  const allApplicationsQuery = useApplicationsQuery();
  const allTransactionsQuery = useTransactionsQuery();
  const allIgnoredFilesQuery = useIgnoredFilesQuery();
  const allEJBsQuery = useEJBsQuery();
  const allJPAsQuery = useJPAsQuery();
  const allHibernateQuery = useHibernateQuery();
  const allSpringBeansQuery = useSpringBeansQuery();
  const allRemoteServicesQuery = useRemoteServicesQuery();
  const allServerResourcesQuery = useServerResourcesQuery();
  const allUnparsableFilesQuery = useUnparsableFilesQuery();
  const allHardcodedIPAddressesQuery = useHardcodedIpAddressesQuery();
  const allCompatibleFilesQuery = useCompatibleFilesQuery();
  const allJBPMsQuery = useJBPMsQuery();

  const application = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    return (
      allApplicationsQuery.data?.find((app) => app.id === applicationId) || null
    );
  }, [routeParams?.params, allApplicationsQuery.data]);

  //

  const ignoredFilesCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allIgnoredFilesQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return data?.ignoredFiles.length ?? 0;
  }, [routeParams?.params, allIgnoredFilesQuery.data]);

  const ejbCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allEJBsQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return (
      (data?.entityBeans.length ?? 0) +
      (data?.messageDrivenBeans.length ?? 0) +
      (data?.sessionBeans.length ?? 0)
    );
  }, [routeParams?.params, allEJBsQuery.data]);

  const jpaCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allJPAsQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return (
      (data?.entities.length ?? 0) +
      (data?.jpaConfigurations.length ?? 0) +
      (data?.namesQueries.length ?? 0)
    );
  }, [routeParams?.params, allJPAsQuery.data]);

  const hibernateCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allHibernateQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return (
      (data?.entities.length ?? 0) + (data?.hibernateConfigurations.length ?? 0)
    );
  }, [routeParams?.params, allHibernateQuery.data]);

  const springBeansCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allSpringBeansQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return data?.beans.length ?? 0;
  }, [routeParams?.params, allSpringBeansQuery.data]);

  const remoteServicesCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allRemoteServicesQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return (
      (data?.ejbRemoteServices.length ?? 0) +
      (data?.jaxRsServices.length ?? 0) +
      (data?.jaxWsServices.length ?? 0) +
      (data?.rmiServices.length ?? 0)
    );
  }, [routeParams?.params, allRemoteServicesQuery.data]);

  const serverResourcesCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allServerResourcesQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return (
      (data?.datasources.length ?? 0) +
      (data?.jmsConnectionFactories.length ?? 0) +
      (data?.jmsDestinations.length ?? 0) +
      (data?.otherJndiEntries.length ?? 0) +
      (data?.threadPools.length ?? 0)
    );
  }, [routeParams?.params, allServerResourcesQuery.data]);

  const unparsableFilesCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allUnparsableFilesQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return data?.subProjects.length ?? 0;
  }, [routeParams?.params, allUnparsableFilesQuery.data]);

  const harcodedIpAddressesCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allHardcodedIPAddressesQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return data?.files.length ?? 0;
  }, [routeParams?.params, allHardcodedIPAddressesQuery.data]);

  const compatibleFilesCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allCompatibleFilesQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return data?.artifacts.length ?? 0;
  }, [routeParams?.params, allCompatibleFilesQuery.data]);

  const jbpmCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allJBPMsQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return data?.jbpms.length ?? 0;
  }, [routeParams?.params, allJBPMsQuery.data]);

  const transactionsCount = useMemo(() => {
    const applicationId = routeParams?.params.applicationId;
    const data = allTransactionsQuery.data?.find(
      (app) => app.applicationId === applicationId
    );
    return data?.transactions.length ?? 0;
  }, [routeParams?.params, allTransactionsQuery.data]);

  const tabItems = useMemo(() => {
    const result: { title: string; path: string }[] = [
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
    ];

    if (ignoredFilesCount > 0) {
      result.push({
        title: "Ignored files",
        path: `/applications/${application?.id}/ignored-files`,
      });
    }
    if (ejbCount > 0) {
      result.push({
        title: "EJB",
        path: `/applications/${application?.id}/ejb`,
      });
    }
    if (jpaCount > 0) {
      result.push({
        title: "JPA",
        path: `/applications/${application?.id}/jpa`,
      });
    }
    if (hibernateCount > 0) {
      result.push({
        title: "Hibernate",
        path: `/applications/${application?.id}/hibernate`,
      });
    }
    if (springBeansCount > 0) {
      result.push({
        title: "Spring beans",
        path: `/applications/${application?.id}/spring-beans`,
      });
    }
    if (remoteServicesCount > 0) {
      result.push({
        title: "Remote services",
        path: `/applications/${application?.id}/remote-services`,
      });
    }
    if (serverResourcesCount > 0) {
      result.push({
        title: "Server resources",
        path: `/applications/${application?.id}/server-resources`,
      });
    }
    if (unparsableFilesCount > 0) {
      result.push({
        title: "Unparsable files",
        path: `/applications/${application?.id}/unparsable-files`,
      });
    }
    if (harcodedIpAddressesCount > 0) {
      result.push({
        title: "Hard-coded IP addresses",
        path: `/applications/${application?.id}/hardcoded-ip-addresses`,
      });
    }
    if (compatibleFilesCount > 0) {
      result.push({
        title: "Compatible files",
        path: `/applications/${application?.id}/compatible-files`,
      });
    }
    if (jbpmCount > 0) {
      result.push({
        title: "JBPM Processes",
        path: `/applications/${application?.id}/jbpm`,
      });
    }

    if (transactionsCount > 0) {
      result.push({
        title: "Transactions",
        path: `/applications/${application?.id}/transactions`,
      });
    }

    return result;
  }, [
    application,
    ignoredFilesCount,
    ejbCount,
    jpaCount,
    hibernateCount,
    springBeansCount,
    remoteServicesCount,
    serverResourcesCount,
    unparsableFilesCount,
    harcodedIpAddressesCount,
    compatibleFilesCount,
    jbpmCount,
    transactionsCount,
  ]);

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
