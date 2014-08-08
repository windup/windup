package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.context.Context;

public class PersistenceConfig extends WindupRuleProvider
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
        
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Session.find", "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use createQuery()", 2, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Session.iterate", "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use createQuery()", 2, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Session.filter", "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use createQuery()", 2, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Session.delete", "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use createQuery()", 2, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Session.saveOrUpdateCopy", "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use merge()", 1, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Session.createSQLQuery", "Deprecated by Hibernate 3, moved to org.hibernate.classic", 3, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Lifecycle", "Deprecated by Hibernate 3, moved to org.hibernate.classic", 3, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Validatable", "Deprecated by Hibernate 3, moved to org.hibernate.classic", 3, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.PersistentEnum", "Removed in Hibernate 3, use UserType", 1, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.FetchMode.EAGER", "Deprecated in Hibernate 3, use FetchMode.JOIN", 1, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.FetchMode.LAZY", "Deprecated in Hibernate 3, use FetchMode.SELECT", 1, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.+", "Replace net.sf.hibernate with org.hibernate (Hibernate 3)", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Interceptor", "Hibernate 3 adds two methods to the Interceptor interface; consider simply extending the EmptyInterceptor class rather than writing empty implementations", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.Interceptor.instantiate\\(\\)", "Method signature is now: instantiate(String entity)", 0, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.isUnsaved\\(\\)", "Renamed to isTransient()", 0, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.UserType", "Re-implement with additional methods, moved to org.hibernate.usertype", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.CompositeUserType", "Re-implement with additional methods, moved to org.hibernate.usertype", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.criterion", "Has undergone significant refactoring, be careful during migration", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.mapping", "Has undergone significant refactoring, be careful during migration", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.persister", "Has undergone significant refactoring, be careful during migration", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "net.sf.hibernate.collection", "Has undergone significant refactoring, be careful during migration", 0, Types.add(TypeReferenceLocation.IMPORT))); 
        
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new JavaScanner().add(hints));
        return configuration;
        */
        return null;
    }
    // @formatter:on
}