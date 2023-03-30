export const ALL_SUPPORTED_ISSUE_CATEGORY = [
  "mandatory",
  "optional",
  "potential",
  "information",
  "cloud-mandatory",
  "cloud-optional",
] as const;
export type IssueCategoryType = typeof ALL_SUPPORTED_ISSUE_CATEGORY[number];

export const issueCategoryTypeBeautifier = (val: IssueCategoryType) => {
  switch (val) {
    case "mandatory":
      return "Migration mandatory";
    case "optional":
      return "Migration optional";
    case "potential":
      return "Migration potential";
    case "information":
      return "Information";
    case "cloud-mandatory":
      return "Cloud mandatory";
    case "cloud-optional":
      return "Cloud optional";
    default:
      return val;
  }
};

export const ALL_LEVEL_OF_EFFORTS = [
  "Info",
  "Trivial",
  "Complex",
  "Redesign",
  "Architectural",
  "Unknown",
] as const;
export type LevelOfEffortType = typeof ALL_LEVEL_OF_EFFORTS[number];

//

const getCategoryPriority = (category: IssueCategoryType) => {
  switch (category) {
    case "mandatory":
      return 1;
    case "optional":
      return 2;
    case "potential":
      return 3;
    case "cloud-mandatory":
      return 4;
    case "cloud-optional":
      return 5;
    case "information":
      return 6;
    default:
      return 0;
  }
};

export function compareByCategoryFn<T>(
  categoryExtractor: (elem: T) => IssueCategoryType
) {
  return (a: T, b: T) => {
    return (
      getCategoryPriority(categoryExtractor(a)) -
      getCategoryPriority(categoryExtractor(b))
    );
  };
}

export function compareByCategoryAndNameFn<T>(
  categoryFn: (elem: T) => IssueCategoryType,
  nameFn: (elem: T) => string
) {
  return (a: T, b: T) => {
    return (
      getCategoryPriority(categoryFn(a)) - getCategoryPriority(categoryFn(b)) ||
      nameFn(a).localeCompare(nameFn(b))
    );
  };
}

//

const getEffortPriority = (effort: LevelOfEffortType) => {
  switch (effort) {
    case "Info":
      return 1;
    case "Trivial":
      return 2;
    case "Complex":
      return 3;
    case "Redesign":
      return 4;
    case "Architectural":
      return 5;
    case "Unknown":
      return 6;
    default:
      return 0;
  }
};

export function compareByEffortFn<T>(
  effortExtractor: (elem: T) => LevelOfEffortType
) {
  return (a: T, b: T) => {
    return (
      getEffortPriority(effortExtractor(a)) -
      getEffortPriority(effortExtractor(b))
    );
  };
}

//

export interface ApplicationIssuesDto {
  applicationId: string;
  issues: {
    [category in IssueCategoryType]: IssueDto[];
  };
}

export interface IssueDto {
  id: string;
  name: string;
  ruleId: string;
  effort: {
    type: LevelOfEffortType;
    points: number;
    description: string;
  };
  totalIncidents: number;
  totalStoryPoints: number;
  links: LinkDto[];
  affectedFiles: IssueAffectedFilesDto[];
  sourceTechnologies?: string[];
  targetTechnologies?: string[];
}

export interface IssueAffectedFilesDto {
  description?: string;
  files: IssueFileDto[];
}

export interface IssueFileDto {
  fileId: string;
  fileName: string;
  occurrences: number;
}

export interface LinkDto {
  title: string;
  href: string;
}
