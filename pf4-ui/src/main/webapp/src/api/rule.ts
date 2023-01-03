export interface RuleGroupDto {
  [key: string]: RuleDto[];
}

export interface RuleDto {
  id: string;
  sourceTechnology?: TechnologyDto[];
  targetTechnology?: TechnologyDto[];
}

export interface TechnologyDto {
  id: string;
  versionRange?: string;
}
