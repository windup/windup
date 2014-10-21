package org.jboss.windup.rules.apps.java.rules;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.liftandshift.service.LiftAndShiftService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Marks Java files as having proprietary content if they reference code from certain packages.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class ProprietaryClassRuleProvider extends WindupRuleProvider
{

    //@formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references(".*\\.ibm\\..*"))
                    .perform(Classification.as(LiftAndShiftService.CLASSIFICATION_CONTAINS_PROPRIETARY))

                    .addRule()
                    .when(JavaClass.references(".*\\.mulesource\\..*"))
                    .perform(Classification.as(LiftAndShiftService.CLASSIFICATION_CONTAINS_PROPRIETARY))

                    .addRule()
                    .when(JavaClass.references(".*\\.bea\\..*"))
                    .perform(Classification.as(LiftAndShiftService.CLASSIFICATION_CONTAINS_PROPRIETARY))

                    .addRule()
                    .when(JavaClass.references(".*\\.weblogic\\..*")
                                   .or(JavaClass.references("weblogic\\..*"))
                    )
                    .perform(Classification.as(LiftAndShiftService.CLASSIFICATION_CONTAINS_PROPRIETARY))

                    .addRule()
                    .when(JavaClass.references(".*\\.ilog\\..*"))
                    .perform(Classification.as(LiftAndShiftService.CLASSIFICATION_CONTAINS_PROPRIETARY));
    }
  //@formatter:on
}
