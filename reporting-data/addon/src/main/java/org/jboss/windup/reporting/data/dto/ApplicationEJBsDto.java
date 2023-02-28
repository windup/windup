package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationEJBsDto {
    private String applicationId;
    private List<EntityBeanDto> entityBeans;
    private List<SessionBeanDto> sessionBeans;
    private List<MessageDrivenBeanDto> messageDrivenBeans;

    public enum SessionBeanType {
        STATELESS,
        STATEFUL
    }

    @Data
    public static abstract class BeanDto {
        private String beanName;
        private String className;

        private String classFileId;
        private String beanDescriptorFileId;
    }

    @Data
    public static class EntityBeanDto extends BeanDto {
        private String tableName;
        private String persistenceType;
    }

    @Data
    public static class SessionBeanDto extends BeanDto {
        private SessionBeanType type;
        private String homeEJBFileId;
        private String localEJBFileId;
        private String remoteEJBFileId;
        private String jndiLocation;
    }

    @Data
    public static class MessageDrivenBeanDto extends BeanDto {
        private String jmsDestination;
    }

}
