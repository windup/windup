package org.jboss.windup.reporting.data.dto;

import java.util.List;
import java.util.Map;

public class ApplicationHibernateDto {
    public String applicationId;
    public List<HibernateEntityDto> entities;
    public List<HibernateConfigurationDto> hibernateConfigurations;

    public static class HibernateEntityDto {
        public String className;
        public String classFileId;
        public String tableName;
    }

    public static class HibernateConfigurationDto {
        public String path;
        public List<HibernateSessionFactoryDto> sessionFactories;
    }

    public static class HibernateSessionFactoryDto {
        public Map<String, String> properties;
    }
}
