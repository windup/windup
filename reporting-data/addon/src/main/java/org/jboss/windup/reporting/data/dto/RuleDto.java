package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class RuleDto {
    private String id;
    private String ruleSetId;
    private Integer verticesAdded;
    private Integer verticesRemoved;
    private Integer edgesAdded;
    private Integer edgesRemoved;
    private boolean executed;
    private boolean failed;
    private String failureMessage;
    private List<TechnologyDto> sourceTechnology;
    private List<TechnologyDto> targetTechnology;

    @Data
    public static class TechnologyDto {
        private String id;
        private String versionRange;
    }
}
