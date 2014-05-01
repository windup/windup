/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config;

import javax.enterprise.inject.Vetoed;

import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.operation.Log;
import org.jboss.windup.addon.config.selectables.JavaClass;
import org.jboss.windup.addon.config.selectables.JavaMethod;
import org.jboss.windup.addon.config.selectables.Selection;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Vetoed
public class DefaultConfigurationProvider extends WindupConfigurationProvider
{
    
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        /*
         * Begin the current rules configuration
         */
        return ConfigurationBuilder.begin()
                    .addRule()
                    
                    /*
                     * Specify a set of conditions that must be met in order for the .perform() clause of this rule to be evaluated.
                     */
                    .when(
                                /*
                                 * Select all java classes with the FQCN matching "javax.(.*)", store the resultant list in a parameter named "types"
                                 */
                                Selection.exists(JavaClass.class, "types").named("javax.{*}")
                    )
                    
                    /*
                     * If all conditions of the .when() clause were satisfied, the following conditions will be evaluated
                     */
                    .perform(
                                /*
                                 * Iterate over the list of java types that were selected in the .when() clause. 
                                 * Each iteration sets the current JavaClass into var "type", and into the "current scope" for the JavaClass type.
                                 */
                                Iteration.over(JavaClass.class, "types", "type")
                                
                                    .when( 
                                                /*
                                                 * Locate methods in the current JavaClass that match the given conditions. 
                                                 * The matching methods are stored into the var "methods"
                                                 */
                                                Selection.exists(JavaMethod.class, "methods")
                                                         .in(Selection.current(JavaClass.class))
                                                         .withSignature("toString()")
                                                         .definedBy("java.lang.Object") 
                                    )
                                    .perform(
                                                /*
                                                 * Iterate over the JavaMethod instances found in "methods", storing each instance in var "method"
                                                 */
                                                Iteration.over(JavaMethod.class, "methods", "method")
                                                    /*
                                                     * Skip directly to perform, no conditions required
                                                     */
                                                   .perform(
                                                        /*
                                                         * Do something with the information we've found so far
                                                         */
                                                        Log.message(Level.INFO, "Overridden " + Selection.get(JavaMethod.class, "method").getName() + 
                                                                    " Method in type: " + Selection.current(JavaClass.class).getName())
                                                   )
                                    )
                    );
    }

}
