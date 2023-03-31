export interface ApplicationServerResourcesDto {
  applicationId: string;
  datasources: DatasourceDto[];
  jmsDestinations: JMSDestinationDto[];
  jmsConnectionFactories: JMSConnectionFactoryDto[];
  threadPools: ThreadPoolDto[];
  otherJndiEntries: OtherJndiEntryDto[];
}

export interface LinkDto {
  link: string;
  description: string;
}

export interface DatasourceDto {
  jndiLocation: string;
  databaseTypeName: string;
  databaseTypeVersion: string;
  links?: LinkDto[];
}

export interface JMSDestinationDto {
  jndiLocation: string;
  destinationType: string;
  links?: LinkDto[];
}

export interface JMSConnectionFactoryDto {
  jndiLocation: string;
  connectionFactoryType: string;
  links?: LinkDto[];
}

export interface ThreadPoolDto {
  poolName: string;
  maxPoolSize: number;
  minPoolSize: string;
  links?: LinkDto[];
}

export interface OtherJndiEntryDto {
  jndiLocation: string;
}
