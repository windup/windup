package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationSpringBeansDto {
    private String applicationId;
    private List<SpringBeanDto> beans;

    @Data
    public static class SpringBeanDto {
        private String beanName;
        private String className;

        private String classFileId;
        private String beanDescriptorFileId;
    }

}
