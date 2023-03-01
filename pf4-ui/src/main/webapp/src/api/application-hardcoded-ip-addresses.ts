export interface ApplicationHardcodedIpAddressesDto {
  applicationId: string;
  files: FileDto[];
}

export interface FileDto {
  fileId?: string;
  lineNumber: number;
  columnNumber: number;
  ipAddress: string;
}
