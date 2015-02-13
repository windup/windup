import org.jboss.windup.config.operation.GraphOperation;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.phase.PostMigrationRules;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.True;
import org.ocpsoft.rewrite.context.EvaluationContext;


ruleSet("ExampleJavaGroovy").setPhase(PostMigrationRules.class)

    .addRule()
    .when(
        JavaClass.references("org.jboss.forge.furnace.{*}").at(TypeReferenceLocation.IMPORT)
    )
    .perform(
         Classification.as("Furnace Service").with(Link.to("JBoss Forge", "http://forge.jboss.org")).withEffort(0)
                    .and(Hint.withText("Furnace type references imply that the client code must be run within a Furnace container.")
                             .withEffort(8))
    )
    .withMetadata(RuleMetadata.CATEGORY, "Basic")
    
