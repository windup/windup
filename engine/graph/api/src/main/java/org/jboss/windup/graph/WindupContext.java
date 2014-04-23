package org.jboss.windup.graph;

import java.io.File;
import java.util.Set;

public interface WindupContext
{
    public GraphContext getGraphContext();
    public File getRunDirectory();
    public Set<String> getPackagesToProfile();
}
