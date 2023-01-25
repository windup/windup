import React from "react";
import { NavLink } from "react-router-dom";

import { Nav, NavList, PageSidebar } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import { useSimpleContext } from "@app/context/simple-context";

import { LayoutTheme } from "./layout-constants";

export const SidebarApp: React.FC = () => {
  const { currentContext } = useSimpleContext();

  const renderPageNav = () => {
    return (
      <Nav id="nav-sidebar" aria-label="Nav" theme={LayoutTheme}>
        <NavList>
          <NavLink
            to="/applications"
            className={({ isActive }) =>
              css("pf-c-nav__link", isActive ? "pf-m-current" : "")
            }
          >
            Applications
          </NavLink>
        </NavList>
        <NavList>
          <NavLink
            to={
              !currentContext
                ? "/issues"
                : "/issues/applications/" + currentContext.key
            }
            className={({ isActive }) =>
              css("pf-c-nav__link", isActive ? "pf-m-current" : "")
            }
          >
            Issues
          </NavLink>
        </NavList>
        <NavList>
          <NavLink
            to={
              !currentContext
                ? "/technologies"
                : "/technologies/applications/" + currentContext.key
            }
            className={({ isActive }) =>
              css("pf-c-nav__link", isActive ? "pf-m-current" : "")
            }
          >
            Technologies
          </NavLink>
        </NavList>
        <NavList>
          <NavLink
            to={
              !currentContext
                ? "/dependencies"
                : "/dependencies/applications/" + currentContext.key
            }
            className={({ isActive }) =>
              css("pf-c-nav__link", isActive ? "pf-m-current" : "")
            }
          >
            Dependencies
          </NavLink>
        </NavList>
        <NavList>
          <NavLink
            to="/rules"
            className={({ isActive }) =>
              css("pf-c-nav__link", isActive ? "pf-m-current" : "")
            }
          >
            Rules
          </NavLink>
        </NavList>
      </Nav>
    );
  };

  return <PageSidebar nav={renderPageNav()} theme={LayoutTheme} />;
};
