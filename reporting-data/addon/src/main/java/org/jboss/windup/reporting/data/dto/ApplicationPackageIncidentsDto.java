package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ApplicationPackageIncidentsDto {
    private String applicationId;
    private Map<String, Integer> packages;
}
