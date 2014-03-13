package org.jboss.windup.addon.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.ContextBase;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.context.RewriteState;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;

class EvaluationContextImpl extends ContextBase implements EvaluationContext
{
   private final List<Operation> preOperations = new ArrayList<>();
   private final List<Operation> postOperations = new ArrayList<>();
   private RewriteState state;

   public EvaluationContextImpl()
   {
      put(ParameterStore.class, new DefaultParameterStore());
   }

   @Override
   public void addPreOperation(final Operation operation)
   {
      this.preOperations.add(operation);
   }

   @Override
   public void addPostOperation(final Operation operation)
   {
      this.preOperations.add(operation);
   }

   /**
    * Get an immutable view of the added pre-{@link Operation} instances.
    */
   public List<Operation> getPreOperations()
   {
      return Collections.unmodifiableList(preOperations);
   }

   /**
    * Get an immutable view of the added post-{@link Operation} instances.
    */
   public List<Operation> getPostOperations()
   {
      return Collections.unmodifiableList(postOperations);
   }

   @Override
   public String toString()
   {
      return "EvaluationContextImpl [preOperations=" + preOperations + ", postOperations=" + postOperations + "]";
   }

   /**
    * Clears the state of this context so that it may be reused, saving instantiation cost during rule iteration.
    */
   public void clear()
   {
      this.postOperations.clear();
      this.postOperations.clear();
   }

   @Override
   public RewriteState getState()
   {
      return state;
   }

   public void setState(RewriteState state)
   {
      this.state = state;
   }
}