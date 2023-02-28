package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationDetailsDto {
    private String applicationId;
    private List<MessageDto> messages;
    private List<ApplicationFileDto> applicationFiles;

    @Data
    public static class MessageDto {
        private String value;
        private String ruleId;
    }

    @Data
    public static class ApplicationFileDto {
        private String fileId;
        private String fileName;
        private String rootPath;
        private int storyPoints;
        private MavenDto maven;
        private List<String> childrenFileIds;
    }

    @Data
    public static class MavenDto {
        private String name;
        private String mavenIdentifier;
        private String projectSite;
        private String sha1;
        private String version;
        private String description;
        private List<String> organizations;
        private List<String> duplicatePaths;
    }
}
