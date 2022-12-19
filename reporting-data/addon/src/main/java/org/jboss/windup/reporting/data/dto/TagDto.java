package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class TagDto {
    public String name;
    public String title;
    public boolean isRoot;
    public boolean isPseudo;
    public List<String> parentsTagNames;
}
