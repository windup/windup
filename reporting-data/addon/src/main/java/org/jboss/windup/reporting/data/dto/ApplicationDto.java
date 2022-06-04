package org.jboss.windup.reporting.data.dto;

import java.util.Map;
import java.util.Set;

public class ApplicationDto {
    public String id;
    public String name;
    public Set<String> tags;
    public int storyPoints;
    public Map<String, Integer> incidents;
}
