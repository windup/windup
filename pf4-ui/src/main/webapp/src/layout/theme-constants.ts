
import mtaNavBrandImage from "@app/images/mta-logo-header.svg";
import mtaLogo from "@app/images/mta-logo.svg";
import tackleFavicon from "@app/images/tackle-favicon.png";
import tackleNavBrandImage from "@app/images/tackle-logo-header.svg";
import tackleLogo from "@app/images/tackle-logo.png";
import windupNavBrandImage from "@app/images/windup-logo-header.svg";
import windupLogo from "@app/images/windup-logo.svg";

type ThemeType = "windup" | "mta" | "tackle";
const defaultTheme: ThemeType = "windup";

type ThemeListType = {
  [key in ThemeType]: {
    name: string;
    logoSrc: string;
    logoNavbarSrc: string;
    faviconSrc?: string;
  };
};

const themeList: ThemeListType = {
  windup: {
    name: "Windup",
    logoSrc: windupLogo,
    logoNavbarSrc: windupNavBrandImage,
  },
  mta: {
    name: "Migration Toolkit for Applications",
    logoSrc: mtaLogo,
    logoNavbarSrc: mtaNavBrandImage,
  },
  tackle: {
    name: "Tackle Analysis",
    logoSrc: tackleLogo,
    logoNavbarSrc: tackleNavBrandImage,
    faviconSrc: tackleFavicon,
  },
};

export const Theme =
  themeList[(process.env.REACT_APP_THEME as ThemeType) || defaultTheme];
