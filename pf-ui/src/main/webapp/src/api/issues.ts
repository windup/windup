export const ALL_SUPPORTED_ISSUE_CATEGORY = [
  "mandatory",
  "optional",
  "potential",
  "information",
  "cloud-mandatory",
  "cloud-optional",
] as const;
export type IssueCategoryType = typeof ALL_SUPPORTED_ISSUE_CATEGORY[number];

export const ALL_LEVEL_OF_EFFORTS = [
  "Info",
  "Trivial",
  "Complex",
  "Redesign",
  "Architectural",
  "Unknown",
] as const;
export type LevelOfEffortType = typeof ALL_LEVEL_OF_EFFORTS[number];

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
