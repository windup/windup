package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationServerResourcesDto {
    private String applicationId;
    private List<DatasourceDto> datasources;
    private List<JMSDestinationDto> jmsDestinations;
    private List<JMSConnectionFactoryDto> jmsConnectionFactories;
    private List<ThreadPoolDto> threadPools;
    private List<OtherJndiEntryDto> otherJndiEntries;

    @Data
    public static class LinkDto {
        private String link;
        private String description;
    }

    @Data
    public static class DatasourceDto {
        private String jndiLocation;
        private String databaseTypeName;
        private String databaseTypeVersion;
        private List<LinkDto> links;
    }

    @Data
    public static class JMSDestinationDto {
        private String jndiLocation;
        private String destinationType;
        private List<LinkDto> links;
    }

    @Data
    public static class JMSConnectionFactoryDto {
        private String jndiLocation;
        private String connectionFactoryType;
        private List<LinkDto> links;
    }

    @Data
    public static class ThreadPoolDto {
        private String poolName;
        private Integer maxPoolSize;
        private Integer minPoolSize;
        private List<LinkDto> links;
    }

    @Data
    public static class OtherJndiEntryDto {
        private String jndiLocation;
    }
}
