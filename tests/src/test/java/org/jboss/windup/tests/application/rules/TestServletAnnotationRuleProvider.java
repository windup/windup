package org.jboss.windup.tests.application.rules;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * This adds a classification to source files that reference the {@link WebServlet} annotation.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class TestServletAnnotationRuleProvider extends WindupRuleProvider
{
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(
                                JavaClass.references("javax.servlet.annotation.WebServlet").at(
                                            TypeReferenceLocation.ANNOTATION)
                    )
                    .perform(
                                Classification.as("Web Servlet")
                                            .with(Link.to("JServlet Help", "http://www.servletsareawesome.com/"))
                                            .withEffort(0)
                                            .and(Hint.withText("Just keep it a Servlet!").withEffort(0))
                    );
    }
}
