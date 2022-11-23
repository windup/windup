package org.jboss.windup.reporting.data.dto;

import org.jboss.windup.reporting.model.TechnologyTagLevel;

import java.util.List;
import java.util.Set;

public class FileDto {
    public String id;
    public String fullPath;
    public String prettyPath;
    public String sourceType;
    public int storyPoints;
    public List<HintDto> hints;
    public List<TagDto> tags;
    public Set<String> classificationsAndHintsTags;

    public static class HintDto {
        public int line;
        public String title;
        public String ruleId;
        public String content;
        public List<ApplicationIssueDto.LinkDto> links;
    }

    public static class TagDto {
        public String name;
        public String version;
        public TechnologyTagLevel level;
    }

}
