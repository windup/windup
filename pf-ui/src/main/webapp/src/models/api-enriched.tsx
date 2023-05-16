import { IssueCategoryType, IssueDto } from "@app/api/issues";
import { RuleDto } from "@app/api/rule";
import { TechnologyGroup } from "@app/api/technologies";

export interface ApplicationIssuesProcessed {
  applicationId: string;
  issues: IssueProcessed[];
}

export interface IssueProcessed extends IssueDto {
  category: IssueCategoryType;
}

export interface RuleProcessed extends RuleDto {
  phase: string;
}

export interface TechnologyTagValue {
  [tagName: string]: number;
}

export interface TechnologyValueProcessed {
  total: number;
  tags: TechnologyTagValue;
}

export interface TechnologyGroupValueProcessed {
  [technologyName: string]: TechnologyValueProcessed;
}

export type TechnologyGroupsProcessed = {
  [groupName in TechnologyGroup]: TechnologyGroupValueProcessed;
};

export interface ApplicationTechnologiesProcessed {
  applicationId: string;
  technologyGroups: TechnologyGroupsProcessed;
}
