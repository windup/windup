export interface ApplicationUnparsableFilesDto {
  applicationId: string;
  subProjects: SubProjectDto[];
}

export interface SubProjectDto {
  path: string;
  unparsableFiles: UnparsableFilesDto[];
}

export interface UnparsableFilesDto {
  fileId: string;
  fileName: string;
  filePath: string;
  parseError?: string;
}
