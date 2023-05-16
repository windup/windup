package org.jboss.windup.reporting.data.dto;

import lombok.Data;
import org.jboss.windup.reporting.model.TechnologyTagLevel;

import java.util.List;
import java.util.Set;

@Data
public class FileDto {
    private String id;
    private String fullPath;
    private String prettyPath;
    private String prettyFileName;
    private String sourceType;
    private int storyPoints;
    private List<HintDto> hints;
    private List<TagDto> tags;
    private Set<String> classificationsAndHintsTags;

    @Data
    public static class HintDto {
        private int line;
        private String title;
        private String ruleId;
        private String content;
        private List<ApplicationIssuesDto.LinkDto> links;
    }

    @Data
    public static class TagDto {
        private String name;
        private String version;
        private TechnologyTagLevel level;
    }

}
