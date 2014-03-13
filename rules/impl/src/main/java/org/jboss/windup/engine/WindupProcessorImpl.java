package org.jboss.windup.engine;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.addon.engine.WindupProcessor;
import org.jboss.windup.engine.provider.ListenerChainProvider;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindupProcessorImpl implements WindupProcessor
{

   private static final Logger LOG = LoggerFactory.getLogger(WindupProcessorImpl.class);

   @Inject
   WindupContext windupContext;

   @Inject
   private ListenerChainProvider provider;

   @Inject
   JavaClassDao javaClassDao;

   @Override
   public void execute()
   {
      List<GraphVisitor> listenerChain = provider.getListenerChain();

      LOG.info("Executing: " + listenerChain.size() + " listeners...");
      for (GraphVisitor visitor : listenerChain)
      {
         LOG.info("Processing: " + visitor.getClass());
         visitor.run();
      }
      LOG.info("Execution complete.");
   }
}
