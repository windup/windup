package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class RuleDto {
    public String id;
    public Integer verticesAdded;
    public Integer verticesRemoved;
    public Integer edgesAdded;
    public Integer edgesRemoved;
    public boolean executed;
    public boolean failed;
    public String failureMessage;
    public List<TechnologyDto> sourceTechnology;
    public List<TechnologyDto> targetTechnology;

    public static class TechnologyDto {
        public String id;
        public String versionRange;
    }
}
