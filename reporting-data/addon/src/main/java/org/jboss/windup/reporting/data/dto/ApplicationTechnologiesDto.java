package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ApplicationTechnologiesDto {
    private String applicationId;
    private Map<String, Map<String, Map<String, Integer>>> technologyGroups;
}
