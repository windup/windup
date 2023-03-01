export interface ApplicationIgnoredFilesDto {
  applicationId: string;
  ignoredFiles: IgnoredFileDto[];
}

export interface IgnoredFileDto {
  fileName: string;
  filePath: string;
  reason: string;
}
