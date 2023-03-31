export interface ApplicationRemoteServicesDto {
  applicationId: string;
  jaxRsServices: JaxRsServiceDto[];
  jaxWsServices: JaxWsServiceDto[];
  ejbRemoteServices: EjbRemoteServiceDto[];
  rmiServices: RmiServiceDto[];
}

export interface JaxRsServiceDto {
  path: string;
  interfaceName: string;
  interfaceFileId: string;
}

export interface JaxWsServiceDto {
  interfaceName: string;
  interfaceFileId: string;
  implementationName: string;
  implementationFileId: string;
}

export interface EjbRemoteServiceDto {
  interfaceName: string;
  interfaceFileId: string;
  implementationName: string;
  implementationFileId: string;
}

export interface RmiServiceDto {
  interfaceName: string;
  interfaceFileId: string;
  implementationName: string;
  implementationFileId: string;
}
