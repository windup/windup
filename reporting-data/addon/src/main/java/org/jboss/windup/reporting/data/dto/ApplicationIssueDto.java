package org.jboss.windup.reporting.data.dto;

import java.util.List;
import java.util.Map;

public class ApplicationIssueDto {
    public String applicationId;
    public Map<String, List<IssueDto>> issues;

    public static class IssueDto {
        public String id;
        public String name;
        public String ruleId;
        public EffortDto effort;
        public int totalIncidents;
        public int totalStoryPoints;
        public List<LinkDto> links;
        public List<IssueAffectedFilesDto> affectedFiles;
    }

    public static class EffortDto {
        public String type;
        public int points;
        public String description;
    }

    public static class LinkDto {
        public String title;
        public String href;
    }

    public static class IssueAffectedFilesDto {
        public String description;
        public List<IssueFileDto> files;
    }

    public static class IssueFileDto {
        public String fileId;
        public String fileName;
        public int occurrences;
    }
}
