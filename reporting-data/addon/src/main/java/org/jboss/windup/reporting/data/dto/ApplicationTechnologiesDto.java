package org.jboss.windup.reporting.data.dto;

import java.util.Map;

public class ApplicationTechnologiesDto {
    public String applicationId;
    public Map<String, Map<String, Map<String, Integer>>> technologyGroups;
}
