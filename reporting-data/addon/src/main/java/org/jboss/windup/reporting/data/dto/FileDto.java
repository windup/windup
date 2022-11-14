package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class FileDto {
    public String id;
    public String fullPath;
    public String prettyPath;
    public String sourceType;
    public List<HintDto> hints;

    public static class HintDto {
        public int line;
        public String title;
        public String ruleId;
        public String content;
        public List<ApplicationIssueDto.LinkDto> links;
    }
}
