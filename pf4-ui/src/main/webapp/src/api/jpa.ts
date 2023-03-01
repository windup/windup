export interface ApplicationJPAsDto {
  applicationId: string;
  entities: JPAEntityDto[];
  namesQueries: JPANamedQueryDto[];
  jpaConfigurations: JPAConfigurationDto[];
}

export interface JPAEntityDto {
  entityName: string;
  tableName: string;
  className: string;
  classFileId?: string;
}

export interface JPANamedQueryDto {
  queryName: string;
  query: string;
}

export interface JPAConfigurationDto {
  path: string;
  version: string;
  persistentUnits: PersistentUnitDto[];
}

export interface PersistentUnitDto {
  name: string;
  properties: { [key: string]: string };
  datasources: DatasourceDto[];
}

export interface DatasourceDto {
  jndiLocation: string;
  databaseTypeName: string;
  isXA: boolean;
}
