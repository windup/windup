package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationSpringBeansDto {
    public String applicationId;
    public List<SpringBeanDto> beans;

    public static class SpringBeanDto {
        public String beanName;
        public String className;

        public String classFileId;
        public String beanDescriptorFileId;
    }

}
