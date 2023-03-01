export interface ApplicationPackageIncidentsDto {
  applicationId: string;
  packages: { [key: string]: number };
}
