package org.jboss.windup.reporting.data.dto;

public class RuleDto {
    public String id;
    public String content;
    public Integer verticesAdded;
    public Integer verticesRemoved;
    public Integer edgesAdded;
    public Integer edgesRemoved;
    public boolean executed;
    public boolean failed;
    public String failureMessage;
}
