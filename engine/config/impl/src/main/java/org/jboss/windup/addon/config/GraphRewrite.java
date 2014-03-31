/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config;

import org.jboss.windup.graph.model.resource.Resource;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.ContextBase;
import org.ocpsoft.rewrite.event.Flow;
import org.ocpsoft.rewrite.event.Rewrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GraphRewrite implements Rewrite
{
   private final Context context = new ContextBase()
   {
   };

   public Resource getResource()
   {
      return null;
   }

   public Logger getLog()
   {
      return LoggerFactory.getLogger(GraphRewrite.class);
   }

   @Override
   public Context getRewriteContext()
   {
      return context;
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
}
