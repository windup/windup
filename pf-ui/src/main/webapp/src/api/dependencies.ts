export interface ApplicationDependenciesDto {
  applicationId: string;
  dependencies: DependencyDto[];
}

export interface DependencyDto {
  name: string;
  version: string;
  mavenIdentifier?: string;
  sha1?: string;
  organization?: string;
  foundPaths: string[];
}
