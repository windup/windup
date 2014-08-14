package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class JBossConfig extends WindupRuleProvider
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
                    JavaClass.references("org.jboss.ejb3.annotation.Management") .at(TypeReferenceLocation.TYPE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "JBoss 5 JMX ManagementBean" ).withEffort( 0
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("javax.jms.QueueConnectionFactory") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "If migrating from JBoss 4, replace lookup string \"QueueConnectionFactory\" with \"ConnectionFactory\"" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.persistence.JoinColumn") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "If migrating from JBoss 4, ensure @JoinColumn is replaced with @JoinColumns({@JoinColumn" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.annotation.ejb.Service") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Migrated to org.jboss.ejb3.annotation.Service" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.annotation.ejb.Management") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Migrated to org.jboss.ejb3.annotation.Management" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.annotation.ejb.LocalBinding") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Migrated to org.jboss.ejb3.annotation.LocalBinding" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jboss.annotation.ejb.Depends") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Validate that JBoss 6 Dependency exists." ).withEffort( 0 )
                    )
                    .endIteration()
                    );

        return configuration;
        // @formatter:on
    }
    // @formatter:on
}
