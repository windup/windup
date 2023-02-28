package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ApplicationDto {
    private String id;
    private String name;
    private boolean isVirtual;
    private Set<String> tags;
    private int storyPoints;
    private int storyPointsInSharedArchives;
    private Map<String, Integer> incidents;
}
