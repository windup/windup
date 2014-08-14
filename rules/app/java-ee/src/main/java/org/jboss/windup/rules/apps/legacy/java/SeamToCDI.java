package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class SeamToCDI extends WindupRuleProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "SEAM Component" ) .with( Link.to( "Seam 2 to Seam 3 Migration Notes" ,"http://www.seamframework.org/Seam3/Seam2ToSeam3MigrationNotes").to( "JSF Web Application Example" ,"http://docs.jboss.org/weld/reference/latest/en-US/html/example.html").to( "JBoss Context Documentation" ,"http://docs.jboss.org/weld/reference/latest/en-US/html/contexts.html").to( "CDI Conversations Blog Post" ,"http://www.andygibson.net/blog/tutorial/cdi-conversations-part-2/") ) .withEffort( 1
                    ))
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Out") .at(TypeReferenceLocation.IMPORT) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Uses Outjection" ) .withEffort( 1
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.core.Conversation") .at(TypeReferenceLocation.IMPORT) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Uses Seam's Conversation object" ) .withEffort( 1
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.core.Context") .at(TypeReferenceLocation.IMPORT) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Uses Seam's Context object" ) .withEffort( 1
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.Seam") .at(TypeReferenceLocation.IMPORT) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Uses Seam's Seam object" ) .withEffort( 1
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.core.ConversationEntries") .at(TypeReferenceLocation.IMPORT) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Uses Seam's ConversationEntries object" ) .withEffort( 1
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.faces.Switcher") .at(TypeReferenceLocation.IMPORT) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Uses Seam's Switcher object" ) .withEffort( 1
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Name") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework injection, note that CDI is static injection. @Named only if accessed in EL" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Scope") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Convert to a valid CDI scope. For example, @Scope(ScopeType.SESSION) should be @javax.enterprise.context.SessionScoped. See the 'Seam 2 to Seam 3 Migration Notes' link." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.In$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Convert Seam @In to CDI @javax.inject.Inject" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Out") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "No Out-jection, rework using @javax.enterprise.inject.Produces. See the 'JSF Web Application Example' link." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Startup") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Use with @javax.ejb.Singleton" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Create") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework with @javax.annotation.PostConstruct" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.international.LocaleSelector") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework with java.util.Locale" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.Component") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace use of getInstance with @javax.inject.Inject." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Redirect") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework so that when the annotated error is thrown, the viewID page is be displayed." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Install") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework with @javax.enterprise.inject.Alternative instead of @Install(false), @Requires instead of dependencies, and @javax.enterprise.inject.Alternative or @javax.enterprise.inject.Specializes instead of precedence. See the 'Seam 2 to Seam 3 Migration Notes' link." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.web.AbstractFilter") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework with a different filter interface" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.core.Conversation") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework with CDI's conversation context" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.contexts.Context") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework using CDI's injected contexts. See the 'JBoss Context Documentation' link." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.Seam") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with CDI functionality" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.core.ConversationEntries") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework. No native CDI support for tracking conversations, but it can be implemented. See the 'CDI Conversations Blog Post' link." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.faces.Switcher") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework. No native CDI support for multiple conversations, but it can be implemented. See the 'CDI Conversations Blog Post' link." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.core.ConversationEntry") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework with CDI conversation context. See the 'JBoss Context Documentation' link." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.Begin") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework with javax.enterprise.context.Conversation.begin()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.seam.annotations.End") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Rework with javax.enterprise.context.Conversation.end()" ).withEffort( 0 )
                    )
                    .endIteration()
                    );

       
        return configuration;
    }
    // @formatter:on
}
