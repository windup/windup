package org.jboss.windup.graph;

import java.io.File;

public interface WindupContext
{
    public GraphContext getGraphContext();
    public File getRunDirectory();
}
