package org.jboss.windup.graph;

import java.nio.file.Path;

public interface GraphContextFactory
{
    GraphContext create(Path runDirectory);

    GraphContext create();
}
