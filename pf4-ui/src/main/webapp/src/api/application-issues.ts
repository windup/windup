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
  description: string;
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
