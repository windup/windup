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
        public String levelOfEffort;
        public int totalStoryPoints;
        public List<LinkDto> links;
        public List<IssueAffectedFilesDto> affectedFiles;
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
