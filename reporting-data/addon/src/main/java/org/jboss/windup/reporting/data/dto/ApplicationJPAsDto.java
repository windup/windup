package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApplicationJPAsDto {
    private String applicationId;
    private List<JPAEntityDto> entities;
    private List<JPANamedQueryDto> namesQueries;
    private List<JPAConfigurationDto> jpaConfigurations;

    @Data
    public static class JPAEntityDto {
        private String entityName;
        private String className;
        private String classFileId;
        private String tableName;
    }

    @Data
    public static class JPANamedQueryDto {
        private String queryName;
        private String query;
    }

    @Data
    public static class JPAConfigurationDto {
        private String path;
        private String version;
        private List<PersistentUnitDto> persistentUnits;
    }

    @Data
    public static class PersistentUnitDto {
        private String name;
        private Map<String, String> properties;
        private List<DatasourceDto> datasources;
    }

    @Data
    public static class DatasourceDto {
        private String jndiLocation;
        private String databaseTypeName;
        private Boolean isXA;
    }

}
