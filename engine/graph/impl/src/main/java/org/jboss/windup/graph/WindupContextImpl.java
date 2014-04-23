package org.jboss.windup.graph;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("windup-context")
@ApplicationScoped
public class WindupContextImpl implements WindupContext
{
    private static final Logger LOG = LoggerFactory.getLogger(WindupContext.class);

    private File runDirectory;
    private GraphContext graphContext;

    public GraphContext getGraphContext()
    {
        if (graphContext == null)
        {
            graphContext = new GraphContextImpl(new File(getRunDirectory(), "windup-graph"));
        }
        return graphContext;
    }

    public File getRunDirectory()
    {
        if (runDirectory == null)
        {
            runDirectory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        }
        return runDirectory;
    }
    
    @Override
    public Set<String> getPackagesToProfile()
    {
        return new HashSet<String>();
    }
    
}
