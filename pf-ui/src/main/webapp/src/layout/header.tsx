import React, { useReducer } from "react";
import { useNavigate } from "react-router-dom";

import {
  Brand,
  Button,
  ButtonVariant,
  Dropdown,
  DropdownItem,
  KebabToggle,
  PageHeader,
  PageHeaderTools,
  PageHeaderToolsGroup,
  PageHeaderToolsItem,
} from "@patternfly/react-core";
import HelpIcon from "@patternfly/react-icons/dist/esm/icons/help-icon";

// import avatarImage from "@app/images/avatar.svg";

import { AboutApp } from "./about";
import { Theme } from "./theme-constants";

export const HeaderApp: React.FC = () => {
  const navigate = useNavigate();

  const [isAboutOpen, toggleIsAboutOpen] = useReducer((state) => !state, false);
  const [isMobileDropdownOpen, toggleIsMobileDropdownOpen] = useReducer(
    (state) => !state,
    false
  );

  return (
    <>
      <AboutApp isOpen={isAboutOpen} onClose={toggleIsAboutOpen} />
      <PageHeader
        logo={<Brand src={Theme.logoNavbarSrc} alt="Brand" />}
        logoProps={{
          onClick: () => navigate("/"),
        }}
        headerTools={
          <PageHeaderTools>
            <PageHeaderToolsGroup
              visibility={{
                default: "hidden",
                lg: "visible",
              }} /** the settings and help icon buttons are only visible on desktop sizes and replaced by a kebab dropdown for other sizes */
            >
              <PageHeaderToolsItem>
                <Button
                  aria-label="About"
                  variant={ButtonVariant.plain}
                  onClick={toggleIsAboutOpen}
                >
                  <HelpIcon />
                </Button>
              </PageHeaderToolsItem>
            </PageHeaderToolsGroup>
            <PageHeaderToolsGroup>
              <PageHeaderToolsItem
                visibility={{
                  lg: "hidden",
                }} /** this kebab dropdown replaces the icon buttons and is hidden for desktop sizes */
              >
                <Dropdown
                  isPlain
                  position="right"
                  toggle={<KebabToggle onToggle={toggleIsMobileDropdownOpen} />}
                  isOpen={isMobileDropdownOpen}
                  // oncl
                  dropdownItems={[
                    <DropdownItem key="about" onClick={toggleIsAboutOpen}>
                      <HelpIcon /> About
                    </DropdownItem>,
                  ]}
                />
              </PageHeaderToolsItem>
              {/* <Avatar src={avatarImage} alt="Avatar image" /> */}
            </PageHeaderToolsGroup>
          </PageHeaderTools>
        }
        showNavToggle
      />
    </>
  );
};
