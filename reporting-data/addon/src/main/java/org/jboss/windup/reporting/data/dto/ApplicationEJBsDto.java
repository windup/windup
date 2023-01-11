package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationEJBsDto {
    public String applicationId;
    public List<BeanDto> beans;

    public enum BeanType {
        MESSAGE_DRIVEN_BEAN,
        STATELESS_SESSION_BEAN,
        STATEFUL_SESSION_BEAN,
        ENTITY_BEAN,
    }

    public static class BeanDto {
        public BeanType type;
        public String beanName;
        public String className;

        public String classFileId;
        public String beanDescriptorFileId;

        public String homeEJBFileId;
        public String localEJBFileId;
        public String remoteEJBFileId;

        public String tableName;
        public String persistenceType;

        public String jmsDestination;
        public List<String> jndiLocations;
    }

}
