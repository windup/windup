import { Suspense, lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

// At least one page should not have lazy() for not having "[mini-css-extract-plugin] Conflicting order." error
import ApplicationList from "./pages/application-list";
const IssuesList = lazy(() => import("./pages/issues-list"));
const TechnologiesList = lazy(() => import("./pages/technologies-list"));
const DependenciesList = lazy(() => import("./pages/dependencies-list"));
const ApplicationEdit = lazy(() => import("./pages/application-edit"));
const ApplicationEditDashboard = lazy(
  () => import("./pages/application-edit/pages/application-dashboard")
);
const ApplicationEditIssues = lazy(
  () => import("./pages/application-edit/pages/application-issues")
);
const ApplicationEditDetails = lazy(
  () => import("./pages/application-edit/pages/application-details")
);
const ApplicationEditTechnologies = lazy(
  () => import("./pages/application-edit/pages/application-technologies")
);
const ApplicationEditDependencies = lazy(
  () => import("./pages/application-edit/pages/application-dependencies")
);
const ApplicationEditIgnoredFiles = lazy(
  () => import("./pages/application-edit/pages/application-ignored-files")
);

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
    // Edit application
    {
      Component: ApplicationEdit,
      path: "/applications/:projectId",
      children: [
        {
          Component: () => <Navigate to="dashboard" replace />,
          path: "",
        },
        {
          Component: ApplicationEditDashboard,
          path: "dashboard",
        },
        {
          Component: ApplicationEditIssues,
          path: "issues",
        },
        {
          Component: ApplicationEditDetails,
          path: "details",
        },
        {
          Component: ApplicationEditTechnologies,
          path: "technologies",
        },
        {
          Component: ApplicationEditDependencies,
          path: "dependencies",
        },
        {
          Component: ApplicationEditIgnoredFiles,
          path: "ignored-files",
        },
      ],
    },
  ];

  return (
    <Suspense fallback={<span>Loading...</span>}>
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
