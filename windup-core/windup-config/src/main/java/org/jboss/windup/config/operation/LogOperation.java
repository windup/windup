/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation;

import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedPatternParser;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class LogOperation extends GraphOperation implements Parameterized
{
   ParameterizedPatternParser message;

   public LogOperation(String message)
   {
      this.message = new RegexParameterizedPatternParser(message);
   }

   @Override
   public void perform(GraphRewrite event, EvaluationContext context)
   {
      event.getLog().info(message.getBuilder().build(event, context));
   }

   @Override
   public Set<String> getRequiredParameterNames()
   {
      return message.getRequiredParameterNames();
   }

   @Override
   public void setParameterStore(ParameterStore store)
   {
      message.setParameterStore(store);
   }
}
