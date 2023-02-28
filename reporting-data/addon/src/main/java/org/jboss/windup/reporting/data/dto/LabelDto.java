package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.Set;

@Data
public class LabelDto {
    private String id;
    private String name;
    private String description;
    private Set<String> supported;
    private Set<String> unsuitable;
    private Set<String> neutral;
}
