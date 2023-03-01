export const ALL_TECHNOLOGY_GROUPS = [
  "View",
  "Connect",
  "Store",
  "Sustain",
  "Execute",
] as const;
export type TechnologyGroup = typeof ALL_TECHNOLOGY_GROUPS[number];

export interface ApplicationTechnologiesDto {
  applicationId: string;
  technologyGroups: { [key in TechnologyGroup]: TechnologyDetailsDto };
}

export type TechnologyDetailsDto = {
  [key: string]: {
    [key: string]: number;
  };
};
