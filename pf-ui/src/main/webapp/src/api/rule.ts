export interface RuleGroupDto {
  [key: string]: RuleDto[];
}

export interface RuleDto {
  id: string;
  ruleSetId: string;
  sourceTechnology?: TechnologyDto[];
  targetTechnology?: TechnologyDto[];
  edgesAdded?: number;
  edgesRemoved?: number;
  verticesAdded?: number;
  verticesRemoved?: number;
  executed?: boolean;
  failed?: boolean;
  failureMessage?: string;
}

export interface TechnologyDto {
  id: string;
  versionRange?: string;
}
