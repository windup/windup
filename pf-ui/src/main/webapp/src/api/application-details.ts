export interface ApplicationDetailsDto {
  applicationId: string;
  messages: {
    value: string;
    ruleId: string;
  }[];
  applicationFiles: ApplicationFileDto[];
}

export interface ApplicationFileDto {
  fileId: string;
  fileName: string;
  rootPath: string;
  storyPoints: number;
  maven: {
    name: string;
    mavenIdentifier: string;
    projectSite?: string;
    sha1: string;
    version: string;
    description: string;
    organizations?: string[];
    duplicatePaths?: string[];
  };
  childrenFileIds: string[];
}
