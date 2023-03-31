package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApplicationHibernateDto {
    private String applicationId;
    private List<HibernateEntityDto> entities;
    private List<HibernateConfigurationDto> hibernateConfigurations;

    @Data
    public static class HibernateEntityDto {
        private String className;
        private String classFileId;
        private String tableName;
    }

    @Data
    public static class HibernateConfigurationDto {
        private String path;
        private List<HibernateSessionFactoryDto> sessionFactories;
    }

    @Data
    public static class HibernateSessionFactoryDto {
        private Map<String, String> properties;
    }
}
