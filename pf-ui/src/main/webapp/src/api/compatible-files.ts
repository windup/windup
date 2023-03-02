export interface ApplicationCompatibleFilesDto {
  applicationId: string;
  artifacts: ArtifactDto[];
}

export interface ArtifactDto {
  name: string;
  files: FileDto[];
}

export interface FileDto {
  fileId?: string;
  fileName: string;
}
