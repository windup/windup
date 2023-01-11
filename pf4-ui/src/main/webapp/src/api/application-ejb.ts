export interface ApplicationEJBsDto {
  applicationId: string;
  entityBeans: EntityBeanDto[];
  sessionBeans: SessionBeanDto[];
  messageDrivenBeans: MessageDrivenBeanDto[];
}

export type SessionBeanType = "STATELESS" | "STATEFUL";

export interface BeanDto {
  beanName: string;
  className: string;
  classFileId?: string;
  beanDescriptorFileId?: string;
}

export interface EntityBeanDto extends BeanDto {
  tableName?: string;
  persistenceType?: string;
}

export interface SessionBeanDto extends BeanDto {
  type: SessionBeanType;
  homeEJBFileId?: string;
  localEJBFileId?: string;
  remoteEJBFileId?: string;
  jndiLocation?: string;
}

export interface MessageDrivenBeanDto extends BeanDto {
  jmsDestination?: string;
}
