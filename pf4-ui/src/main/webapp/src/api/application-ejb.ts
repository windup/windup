export interface ApplicationEJBsDto {
  applicationId: string;
  beans: BeanDto[];
}

export type BeanType =
  | "MESSAGE_DRIVEN_BEAN"
  | "STATELESS_SESSION_BEAN"
  | "STATEFUL_SESSION_BEAN"
  | "ENTITY_BEAN";

export interface BeanDto {
  type: BeanType;
  
  classFileId: string;
  beanDescriptorFileId: string;
  
  homeEJBFileId?: string;
  localEJBFileId?: string;
  remoteEJBFileId?: string;

  beanName: string;
  className: string;

  tableName?: string;
  persistenceType?: string;

  jmsDestination?: string;

  jndiLocations?: string[];
}
