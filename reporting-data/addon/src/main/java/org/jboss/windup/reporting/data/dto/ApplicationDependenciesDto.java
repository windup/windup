package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationDependenciesDto {
    private String applicationId;
    private List<DependencyDto> dependencies;

    @Data
    public static class DependencyDto {
        private String name;
        private String sha1;
        private String version;
        private String organization;
        private String mavenIdentifier;
        private List<String> foundPaths;
    }
}
