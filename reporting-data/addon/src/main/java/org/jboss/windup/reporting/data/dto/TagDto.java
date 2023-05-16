package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class TagDto {
    private String name;
    private String title;
    private Boolean isRoot;
    private Boolean isPseudo;
    private List<String> parentsTagNames;
}
