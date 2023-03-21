import { Suspense, lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import { Bullseye, Spinner } from "@patternfly/react-core";

// At least one page should not have lazy() for not having "[mini-css-extract-plugin] Conflicting order." error
const ApplicationList = lazy(() => import("./pages/application-list"));
const IssuesList = lazy(() => import("./pages/issues-list"));
const TechnologiesList = lazy(() => import("./pages/technologies-list"));
const DependenciesList = lazy(() => import("./pages/dependencies-list"));
const RulesList = lazy(() => import("./pages/rules-list"));
const AppEdit = lazy(() => import("./pages/application-edit"));
const AppEditDashboard = lazy(
  () => import("./pages/application-edit/pages/dashboard")
);
const AppEditIssues = lazy(
  () => import("./pages/application-edit/pages/issues")
);
const AppEditDetails = lazy(
  () => import("./pages/application-edit/pages/details")
);
const AppEditTechnologies = lazy(
  () => import("./pages/application-edit/pages/technologies")
);
const AppEditDependencies = lazy(
  () => import("./pages/application-edit/pages/dependencies")
);
const AppEditIgnoredFiles = lazy(
  () => import("./pages/application-edit/pages/ignored-files")
);
const AppEJB = lazy(() => import("./pages/application-edit/pages/ejb"));
const AppJPA = lazy(() => import("./pages/application-edit/pages/jpa"));
const AppHibernate = lazy(
  () => import("./pages/application-edit/pages/hibernate")
);
const AppSpringBeans = lazy(
  () => import("./pages/application-edit/pages/spring-beans")
);
const AppRemoteServices = lazy(
  () => import("./pages/application-edit/pages/remote-services")
);
const AppServerResources = lazy(
  () => import("./pages/application-edit/pages/server-resources")
);
const AppUnparsableFiles = lazy(
  () => import("./pages/application-edit/pages/unparsable-files")
);
const AppTransactions = lazy(
  () => import("./pages/application-edit/pages/transactions")
);
const AppHardcodedIPAddresses = lazy(
  () => import("./pages/application-edit/pages/hardcoded-ip-addresses")
);
const AppCompatibleFiles = lazy(
  () => import("./pages/application-edit/pages/compatible-files")
);
const AppJBPM = lazy(() => import("./pages/application-edit/pages/jbpm"));

export type ApplicationRoute = {
  applicationId: string;
};

export const AppRoutes = () => {
  const routes = [
    {
      Component: ApplicationList,
      path: "/applications",
      hasDescendant: false,
    },
    // Issues
    {
      Component: IssuesList,
      path: "/issues",
      hasDescendant: false,
    },
    {
      Component: IssuesList,
      path: "/issues/applications",
      hasDescendant: false,
    },
    {
      Component: IssuesList,
      path: "/issues/applications/:applicationId",
      hasDescendant: false,
    },
    // Technologies
    {
      Component: TechnologiesList,
      path: "/technologies",
      hasDescendant: false,
    },
    {
      Component: TechnologiesList,
      path: "/technologies/applications",
      hasDescendant: false,
    },
    {
      Component: TechnologiesList,
      path: "/technologies/applications/:applicationId",
      hasDescendant: false,
    },
    // Dependencies
    {
      Component: DependenciesList,
      path: "/dependencies",
      hasDescendant: false,
    },
    {
      Component: DependenciesList,
      path: "/dependencies/applications",
      hasDescendant: false,
    },
    {
      Component: DependenciesList,
      path: "/dependencies/applications/:applicationId",
      hasDescendant: false,
    },
    // Rules
    {
      Component: RulesList,
      path: "/rules",
      hasDescendant: false,
    },
    // Edit application
    {
      Component: AppEdit,
      path: "/applications/:projectId",
      children: [
        {
          Component: () => <Navigate to="dashboard" replace />,
          path: "",
        },
        {
          Component: AppEditDashboard,
          path: "dashboard",
        },
        {
          Component: AppEditIssues,
          path: "issues",
        },
        {
          Component: AppEditDetails,
          path: "details",
        },
        {
          Component: AppEditTechnologies,
          path: "technologies",
        },
        {
          Component: AppEditDependencies,
          path: "dependencies",
        },
        {
          Component: AppEditIgnoredFiles,
          path: "ignored-files",
        },
        {
          Component: AppEJB,
          path: "ejb",
        },
        {
          Component: AppJPA,
          path: "jpa",
        },
        {
          Component: AppHibernate,
          path: "hibernate",
        },
        {
          Component: AppSpringBeans,
          path: "spring-beans",
        },
        {
          Component: AppRemoteServices,
          path: "remote-services",
        },
        {
          Component: AppServerResources,
          path: "server-resources",
        },
        {
          Component: AppUnparsableFiles,
          path: "unparsable-files",
        },
        {
          Component: AppTransactions,
          path: "transactions",
        },
        {
          Component: AppHardcodedIPAddresses,
          path: "hardcoded-ip-addresses",
        },
        {
          Component: AppCompatibleFiles,
          path: "compatible-files",
        },
        {
          Component: AppJBPM,
          path: "jbpm",
        },
      ],
    },
  ];

  return (
    <Suspense
      fallback={
        <Bullseye>
          <Spinner />
        </Bullseye>
      }
    >
      <Routes>
        {routes.map(({ path, hasDescendant, Component, children }, index) => (
          <Route
            key={index}
            path={!hasDescendant ? path : `${path}/*`}
            element={<Component />}
          >
            {children?.map(
              ({ path: childPath, Component: ChildComponent }, childIndex) => (
                <Route
                  key={childIndex}
                  path={childPath}
                  element={<ChildComponent />}
                />
              )
            )}
          </Route>
        ))}
        <Route path="/" element={<Navigate to="/applications" />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Suspense>
  );
};
