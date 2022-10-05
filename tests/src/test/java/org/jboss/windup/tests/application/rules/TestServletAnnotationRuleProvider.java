package org.jboss.windup.tests.application.rules;

import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * This adds a classification to source files that reference the {@link WebServlet} annotation.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TestServletAnnotationRuleProvider extends AbstractRuleProvider {
    public TestServletAnnotationRuleProvider() {
        super(MetadataBuilder.forProvider(TestServletAnnotationRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
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
