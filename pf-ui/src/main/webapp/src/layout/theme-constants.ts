import mtaFavicon from "@app/images/mta-favicon.png";
import mtaNavBrandImage from "@app/images/mta-logo-header.svg";
import mtaLogo from "@app/images/mta-logo.svg";
import mtrFavicon from "@app/images/mtr-favicon.png";
import mtrNavBrandImage from "@app/images/mtr-logo-header.svg";
import mtrLogo from "@app/images/mtr-logo.svg";
import tackleFavicon from "@app/images/tackle-favicon.png";
import tackleNavBrandImage from "@app/images/tackle-logo-header.svg";
import tackleLogo from "@app/images/tackle-logo.png";
import windupFavicon from "@app/images/windup-favicon.png";
import windupNavBrandImage from "@app/images/windup-logo-header.svg";
import windupLogo from "@app/images/windup-logo.svg";

type ThemeType = "windup" | "mta" | "mtr" | "tackle";
const defaultTheme: ThemeType = "windup";

type ThemeListType = {
  [key in ThemeType]: {
    name: string;
    logoSrc: string;
    logoNavbarSrc: string;
    faviconSrc?: string;
    websiteURL: string;
    documentationURL: string;
  };
};

const themeList: ThemeListType = {
  windup: {
    name: "Windup",
    logoSrc: windupLogo,
    logoNavbarSrc: windupNavBrandImage,
    faviconSrc: windupFavicon,
    websiteURL: "https://windup.github.io/",
    documentationURL: "https://windup.github.io/",
  },
  mta: {
    name: "Migration Toolkit for Applications",
    logoSrc: mtaLogo,
    logoNavbarSrc: mtaNavBrandImage,
    faviconSrc: mtaFavicon,
    websiteURL: "https://developers.redhat.com/products/mta/overview/",
    documentationURL:
        "https://access.redhat.com/documentation/en-us/migration_toolkit_for_applications/",
  },
  mtr: {
    name: "Migration Toolkit for Runtimes",
    logoSrc: mtrLogo,
    logoNavbarSrc: mtrNavBrandImage,
    faviconSrc: mtrFavicon,
    websiteURL: "https://developers.redhat.com/products/mtr/overview/",
    documentationURL:
        "https://access.redhat.com/documentation/en-us/migration_toolkit_for_runtimes/",
  },
  tackle: {
    name: "Tackle Analysis",
    logoSrc: tackleLogo,
    logoNavbarSrc: tackleNavBrandImage,
    faviconSrc: tackleFavicon,
    websiteURL: "https://konveyor.github.io/tackle/",
    documentationURL: "https://konveyor.github.io/tackle/",
  },
};

export const Theme =
  themeList[((window as any)["WINDUP_THEME"] as ThemeType) || defaultTheme];
