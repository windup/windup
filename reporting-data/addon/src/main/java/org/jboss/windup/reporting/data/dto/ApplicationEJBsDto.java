package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationEJBsDto {
    public String applicationId;
    public List<EntityBeanDto> entityBeans;
    public List<SessionBeanDto> sessionBeans;
    public List<MessageDrivenBeanDto> messageDrivenBeans;

    public enum SessionBeanType {
        STATELESS,
        STATEFUL
    }

    public static abstract class BeanDto {
        public String beanName;
        public String className;

        public String classFileId;
        public String beanDescriptorFileId;
    }

    public static class EntityBeanDto extends BeanDto {
        public String tableName;
        public String persistenceType;
    }

    public static class SessionBeanDto extends BeanDto {
        public SessionBeanType type;
        public String homeEJBFileId;
        public String localEJBFileId;
        public String remoteEJBFileId;
        public String jndiLocation;
    }

    public static class MessageDrivenBeanDto extends BeanDto {
        public String jmsDestination;
    }

}
