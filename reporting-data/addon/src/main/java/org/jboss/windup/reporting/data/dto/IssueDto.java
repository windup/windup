package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class IssueDto {
    public String id;
    public String name;
    public String ruleId;
    public String levelOfEffort;
    public List<LinkDto> links;
    public List<IssueAffectedFilesDto> affectedFiles;
}
