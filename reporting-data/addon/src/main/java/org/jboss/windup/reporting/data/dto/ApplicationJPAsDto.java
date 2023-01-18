package org.jboss.windup.reporting.data.dto;

import java.util.List;
import java.util.Map;

public class ApplicationJPAsDto {
    public String applicationId;
    public List<JPAEntityDto> entities;
    public List<JPANamedQueryDto> namesQueries;
    public List<JPAConfigurationDto> jpaConfigurations;

    public static class JPAEntityDto {
        public String entityName;
        public String className;
        public String classFileId;
        public String tableName;
    }

    public static class JPANamedQueryDto {
        public String queryName;
        public String query;
    }

    public static class JPAConfigurationDto {
        public String path;
        public String version;
        public List<PersistentUnitDto> persistentUnits;
    }

    public static class PersistentUnitDto {
        public String name;
        public Map<String, String> properties;
        public List<DatasourceDto> datasources;
    }

    public static class DatasourceDto {
        public String jndiLocation;
        public String databaseTypeName;
        public boolean isXA;
    }

}
