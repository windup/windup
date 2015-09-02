import org.jboss.windup.ast.java.data.TypeReferenceLocation
import org.jboss.windup.config.phase.PostMigrationRulesPhase
import org.jboss.windup.reporting.config.Hint
import org.jboss.windup.reporting.config.Link
import org.jboss.windup.reporting.config.classification.Classification
import org.jboss.windup.rules.apps.java.condition.JavaClass
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterest
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestResolver

TypeInterestResolver.defaultInstance().addTypeInterest(new TypeInterest("org.jboss.forge.furnace"));

ruleSet("ExampleJavaGroovy").setPhase(PostMigrationRulesPhase.class)

    .addRule()
    .when(
        JavaClass.references("org.jboss.forge.furnace.{*}").at(TypeReferenceLocation.IMPORT)
    )
    .perform(
         Classification.as("Furnace Service").with(Link.to("JBoss Forge", "http://forge.jboss.org")).withEffort(0)
                    .and(Hint.withText("Furnace type references imply that the client code must be run within a Furnace container.")
                             .withEffort(8))
    )
    
