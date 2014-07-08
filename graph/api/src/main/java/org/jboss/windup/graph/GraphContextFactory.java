package org.jboss.windup.graph;

import java.io.File;

public interface GraphContextFactory
{
    GraphContext create(File runDirectory);

    GraphContext create();
}
