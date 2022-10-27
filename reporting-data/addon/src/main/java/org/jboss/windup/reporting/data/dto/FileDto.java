package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class FileDto {
    public String id;
    public String fullPath;
    public String prettyPath;
    public String sourceType;
    public List<HintDto> hints;
}
