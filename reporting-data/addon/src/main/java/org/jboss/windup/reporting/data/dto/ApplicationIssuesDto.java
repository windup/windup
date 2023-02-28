package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApplicationIssuesDto {
    private String applicationId;
    private Map<String, List<IssueDto>> issues;

    @Data
    public static class IssueDto {
        private String id;
        private String name;
        private String ruleId;
        private EffortDto effort;
        private int totalIncidents;
        private int totalStoryPoints;
        private List<LinkDto> links;
        private List<IssueAffectedFilesDto> affectedFiles;
    }

    @Data
    public static class EffortDto {
        private String type;
        private int points;
        private String description;
    }

    @Data
    public static class LinkDto {
        private String title;
        private String href;
    }

    @Data
    public static class IssueAffectedFilesDto {
        private String description;
        private List<IssueFileDto> files;
    }

    @Data
    public static class IssueFileDto {
        private String fileId;
        private String fileName;
        private int occurrences;
    }
}
