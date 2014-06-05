/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.condition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.Parameterized;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ParamCondition extends GraphCondition implements Parameterized
{
   private final String name;
   private final String value;

   public ParamCondition(String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   @Override
   public boolean evaluate(GraphRewrite event, EvaluationContext context)
   {
      ParameterStore store = (ParameterStore) context.get(ParameterStore.class);
      ParameterValueStore values = (ParameterValueStore) context.get(ParameterValueStore.class);
      boolean result = values.submit(event, context, store.get(name), value);
      return result;
   }

   @Override
   public Set<String> getRequiredParameterNames()
   {
      return new HashSet<>(Arrays.asList(name));
   }

   @Override
   public void setParameterStore(ParameterStore store)
   {
   }

}
