package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationServerResourcesDto {
    public String applicationId;
    public List<DatasourceDto> datasources;
    public List<JMSDestinationDto> jmsDestinations;
    public List<JMSConnectionFactoryDto> jmsConnectionFactories;
    public List<ThreadPoolDto> threadPools;
    public List<OtherJndiEntryDto> otherJndiEntries;

    public static class LinkDto {
        public String link;
        public String description;
    }

    public static class DatasourceDto {
        public String jndiLocation;
        public String databaseTypeName;
        public String databaseTypeVersion;
        public List<LinkDto> links;
    }

    public static class JMSDestinationDto {
        public String jndiLocation;
        public String destinationType;
        public List<LinkDto> links;
    }

    public static class JMSConnectionFactoryDto {
        public String jndiLocation;
        public String connectionFactoryType;
        public List<LinkDto> links;
    }

    public static class ThreadPoolDto {
        public String poolName;
        public Integer maxPoolSize;
        public Integer minPoolSize;
        public List<LinkDto> links;
    }

    public static class OtherJndiEntryDto {
        public String jndiLocation;
    }
}
