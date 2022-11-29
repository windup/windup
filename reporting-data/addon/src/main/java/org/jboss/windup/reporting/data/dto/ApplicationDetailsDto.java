package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationDetailsDto {
    public String applicationId;
    public List<MessageDto> messages;
    public List<ApplicationFileDto> applicationFiles;

    public static class MessageDto {
        public String value;
        public String ruleId;
    }

    public static class ApplicationFileDto {
        public String fileId;
        public String fileName;
        public String rootPath;
        public int storyPoints;
        public MavenDto maven;
        public List<String> childrenFileIds;
    }

    public static class MavenDto {
        public String name;
        public String mavenIdentifier;
        public String projectSite;
        public String sha1;
        public String version;
        public String description;
        public List<String> organizations;
        public List<String> duplicatePaths;
    }
}
