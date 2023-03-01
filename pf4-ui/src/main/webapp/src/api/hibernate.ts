export interface ApplicationHibernateDto {
  applicationId: string;
  entities: HibernateEntityDto[];
  hibernateConfigurations: HibernateConfigurationDto[];
}

export interface HibernateEntityDto {
  tableName: string;
  className: string;
  classFileId?: string;
}

export interface HibernateConfigurationDto {
  path: string;
  sessionFactories: HibernateSessionFactoryDto[];
}

export interface HibernateSessionFactoryDto {
  properties: { [key: string]: string };
}
