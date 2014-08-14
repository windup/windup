package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class JPPConfig extends WindupRuleProvider
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
                    JavaClass.references("org.exoplatform.web.login.InitiateLoginServlet") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.exoplatform.web.login.DoLoginServlet") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.exoplatform.web.login.ErrorLoginServlet") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.exoplatform.web.security.PortalLoginController") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version." ).withEffort( 0 )
                    )
                    .endIteration()
                    );

       
        return configuration;
    }
    // @formatter:on
}
