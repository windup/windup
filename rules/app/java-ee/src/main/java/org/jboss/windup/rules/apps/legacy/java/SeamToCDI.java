package org.jboss.windup.rules.apps.legacy.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.BlackListRegex;
import org.jboss.windup.rules.apps.java.blacklist.JavaClassification;
import org.jboss.windup.rules.apps.java.blacklist.JavaScanner;
import org.jboss.windup.rules.apps.java.blacklist.Types;
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
     
        List<JavaClassification> classifications = new ArrayList<JavaClassification>();
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        
        classifications.add(new JavaClassification(getID(), "SEAM Component", "org.jboss.seam", 1,null));
        classifications.add(new JavaClassification(getID(), "Uses Outjection", "org.jboss.seam.annotations.Out", 1, Types.add(TypeReferenceLocation.IMPORT)));
        classifications.add(new JavaClassification(getID(), "Uses Seam's Conversation object", "org.jboss.seam.core.Conversation", 1, Types.add(TypeReferenceLocation.IMPORT)));
        classifications.add(new JavaClassification(getID(), "Uses Seam's Context object", "org.jboss.seam.core.Context", 1, Types.add(TypeReferenceLocation.IMPORT)));
        classifications.add(new JavaClassification(getID(), "Uses Seam's Seam object", "org.jboss.seam.Seam", 1, Types.add(TypeReferenceLocation.IMPORT)));
        classifications.add(new JavaClassification(getID(), "Uses Seam's ConversationEntries object", "org.jboss.seam.core.ConversationEntries", 1, Types.add(TypeReferenceLocation.IMPORT)));
        classifications.add(new JavaClassification(getID(), "Uses Seam's Switcher object", "org.jboss.seam.faces.Switcher", 1, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.Name", "Rework injection, note that CDI is static injection. @Named only if accessed in EL", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.Scope", "Convert to a valid CDI scope. For example, @Scope(ScopeType.SESSION) should be @javax.enterprise.context.SessionScoped. See the 'Seam 2 to Seam 3 Migration Notes' link.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.In$", "Convert Seam @In to CDI @javax.inject.Inject", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.Out", "No Out-jection, rework using @javax.enterprise.inject.Produces. See the 'JSF Web Application Example' link.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.Startup", "Use with @javax.ejb.Singleton", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.Create", "Rework with @javax.annotation.PostConstruct", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.international.LocaleSelector", "Rework with java.util.Locale", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.Component", "Replace use of getInstance with @javax.inject.Inject.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.Redirect", "Rework so that when the annotated error is thrown, the viewID page is be displayed.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.Install", "Rework with @javax.enterprise.inject.Alternative instead of @Install(false), @Requires instead of dependencies, and @javax.enterprise.inject.Alternative or @javax.enterprise.inject.Specializes instead of precedence. See the 'Seam 2 to Seam 3 Migration Notes' link.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.web.AbstractFilter", "Rework with a different filter interface", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.core.Conversation", "Rework with CDI's conversation context", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.contexts.Context", "Rework using CDI's injected contexts. See the 'JBoss Context Documentation' link.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.Seam", "Replace with CDI functionality", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.core.ConversationEntries", "Rework. No native CDI support for tracking conversations, but it can be implemented. See the 'CDI Conversations Blog Post' link.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.faces.Switcher", "Rework. No native CDI support for multiple conversations, but it can be implemented. See the 'CDI Conversations Blog Post' link.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.core.ConversationEntry", "Rework with CDI conversation context. See the 'JBoss Context Documentation' link.", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.Begin", "Rework with javax.enterprise.context.Conversation.begin()", 0));
        hints.add(new BlackListRegex(getID(), "org.jboss.seam.annotations.End", "Rework with javax.enterprise.context.Conversation.end()", 0)); 
        
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new JavaScanner().add(classifications).add(hints));
        return configuration;
    }
    // @formatter:on
}
