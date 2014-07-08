/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.AbstractRewrite;
import org.ocpsoft.rewrite.event.Flow;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GraphRewrite extends AbstractRewrite implements Rewrite
{
    private static final String WINDUP_TEMP_PREFIX = "windup";

    private final GraphContext graphContext;
    private Path tempDirectory;

    public GraphRewrite(GraphContext context)
    {
        this.graphContext = context;
    }

    public Path getWindupTemporaryFolder()
    {
        if (this.tempDirectory == null)
        {
            try
            {
                this.tempDirectory = Files.createTempDirectory(WINDUP_TEMP_PREFIX);
            }
            catch (IOException e)
            {
                throw new WindupException("Error creating temporary directory for windup due to: " + e.getMessage(), e);
            }
        }
        return this.tempDirectory;
    }

    public void selectionPush()
    {
        Variables.instance(this).push();
    }

    public void selectionPop()
    {
        Variables.instance(this).pop();
    }

    @Override
    public Flow getFlow()
    {
        return new Flow()
        {

            @Override
            public boolean isHandled()
            {
                return false;
            }

            @Override
            public boolean is(Flow type)
            {
                return false;
            }
        };
    }

    public GraphContext getGraphContext()
    {
        return graphContext;
    }
}
