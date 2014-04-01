package org.jboss.windup.graph;

import java.io.File;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("windup-context")
@ApplicationScoped
public class WindupContext
{
   private static final Logger LOG = LoggerFactory.getLogger(WindupContext.class);

   private File runDirectory;
   private GraphContext graphContext;

   public GraphContext getGraphContext()
   {
      if (graphContext == null)
      {
         graphContext = new GraphContext(new File(getRunDirectory(), "windup-graph"));
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
}
