package org.jboss.windup.reporting.data.dto;

import java.util.Set;

public class LabelDto {
    public String id;
    public String name;
    public String description;
    public Set<String> supported;
    public Set<String> unsuitable;
    public Set<String> neutral;
}
