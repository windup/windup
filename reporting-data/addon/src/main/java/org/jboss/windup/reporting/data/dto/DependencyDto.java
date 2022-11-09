package org.jboss.windup.reporting.data.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyDto {
    public String name;
    public String mavenIdentifier;
    public String sha1;
    public String version;
    public String organization;
    public List<String> foundPaths;
}
