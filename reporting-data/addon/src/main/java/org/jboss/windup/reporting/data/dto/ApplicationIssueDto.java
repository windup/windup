package org.jboss.windup.reporting.data.dto;

import org.jboss.windup.reporting.data.rules.IssuesApiRuleProvider;

import java.util.List;
import java.util.Map;

public class ApplicationIssueDto {
    public String applicationId;
    public Map<String, List<IssueDto>> issues;
}
