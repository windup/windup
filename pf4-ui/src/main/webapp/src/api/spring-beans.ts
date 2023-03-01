export interface ApplicationSpringBeansDto {
  applicationId: string;
  beans: SpringBeanDto[];
}

export interface SpringBeanDto {
  beanName: string;
  className: string;
  classFileId?: string;
  beanDescriptorFileId?: string;
}
