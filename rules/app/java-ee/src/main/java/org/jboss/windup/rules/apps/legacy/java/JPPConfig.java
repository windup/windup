package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
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
        /* TODO Change to use new Hints/classifications API
        
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        
        hints.add(new BlackListRegex(getID(), "org.exoplatform.web.login.InitiateLoginServlet", "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.exoplatform.web.login.DoLoginServlet", "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.exoplatform.web.login.ErrorLoginServlet", "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.exoplatform.web.security.PortalLoginController", "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.", 0, Types.add(TypeReferenceLocation.IMPORT))); 
        
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule().perform(new JavaScanner().add(hints));
        return configuration;
       */
        return null;
    }
    // @formatter:on
}
