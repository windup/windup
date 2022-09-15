package org.jboss.windup.tooling;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ToolingModeData {
    public Set<String> input;
    public String output;
    public boolean sourceMode;
    public boolean ignoreReport;
    public List<String> ignorePatterns;
    public String windupHome;
    public List<String> source;
    public List<String> target;
    public List<File> rulesDir;
}
