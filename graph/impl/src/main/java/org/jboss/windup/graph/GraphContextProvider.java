package org.jboss.windup.graph;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GraphContextProvider
{

   @Inject
   private WindupContext context;

   @Produces
   public GraphContext produceGraphContext()
   {
      return context.getGraphContext();
   }

}