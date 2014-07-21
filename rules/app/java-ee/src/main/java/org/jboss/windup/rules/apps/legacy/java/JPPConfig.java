package org.jboss.windup.rules.apps.legacy.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.BlackListRegex;
import org.jboss.windup.rules.apps.java.blacklist.ASTEventEvaluatorsBufferOperation;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
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

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
     
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        
        hints.add(new BlackListRegex(getID(), "org.exoplatform.web.login.InitiateLoginServlet", "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.", 0, Types.add(ClassCandidateType.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.exoplatform.web.login.DoLoginServlet", "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.", 0, Types.add(ClassCandidateType.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.exoplatform.web.login.ErrorLoginServlet", "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.", 0, Types.add(ClassCandidateType.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.exoplatform.web.security.PortalLoginController", "This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.", 0, Types.add(ClassCandidateType.IMPORT))); 
        
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule().perform(new ASTEventEvaluatorsBufferOperation().add(hints));
        return configuration;
        
    }
}
